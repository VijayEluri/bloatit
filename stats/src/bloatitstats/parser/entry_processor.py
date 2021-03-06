import re
from uasparser import UASparser
from urlparse import urlparse

#set a custom writable cache folder or use the folder where the script locate 
uas_parser = UASparser('/tmp')

class entry_processor:

    nb_new_request = 0
    nb_new_visit = 0
    nb_new_visitor = 0
    nb_new_linkable = 0
    nb_new_useragent = 0
    nb_new_referer = 0
    nb_skipped = 0
    first_request_date = None
    last_request_date = None
    ua_data = {}
    ref_data = {}
    visitor_data = {}
    request_data={}
    pagename_data={}
        

    def __init__(self, date, thread, level):
        self.date = date
        self.thread = thread
        self.level = level
        
        self.user_id = -1
        self.key = ''
        self.lang = ''
        
        self.request_uri = ''
        self.request_method = ''
        self.user_agent = ''
        self.accept_languages = ''
        self.http_referer = ''
        self.remote_addr = ''
        self.server_protocol = ''
        self.server_addr = ''
        
        
    def add_context(self, context):
        self.user_id = context.user_id
        self.key = context.key
        self.lang = context.lang
        
    def add_request(self, request):
        self.request_uri = request.request_uri
        self.request_method = request.request_method
        self.user_agent = request.user_agent
        self.accept_languages = request.accept_languages
        self.http_referer = request.http_referer
        self.remote_addr = request.remote_addr
        self.server_protocol = request.server_protocol
        self.server_addr = request.server_addr
        
    def process(self, cursor):
        # verify that this entry has not been parsed yet
        cursor.execute('SELECT id FROM meta WHERE last_parsed_entry_date >= datetime(?)', (self.date,))
        is_already_parse = cursor.fetchone()
        if is_already_parse:
            entry_processor.nb_skipped += 1
            return
        
        if not entry_processor.first_request_date:
            entry_processor.first_request_date = self.date
        entry_processor.last_request_date = self.date
        
        # Create linkable
        pageurl = urlparse(self.request_uri)
        if pageurl.path in entry_processor.pagename_data:
            linkable_id = entry_processor.pagename_data[pageurl.path]
        else:
            cursor.execute('SELECT id FROM linkable WHERE pagename=?;', (pageurl.path,))
            linkable_id = cursor.fetchone()
            if not linkable_id:
                cursor.execute('''INSERT INTO linkable (pagename, isAction, isRest, isadmin) VALUES (?, 'false', 'false', 'false')''', (pageurl.path,))
                linkable_id = cursor.lastrowid
                entry_processor.nb_new_linkable += 1
            else:
                linkable_id = linkable_id[0]
            entry_processor.pagename_data[pageurl.path] = linkable_id
        
        # create visitor
        cursor.execute('SELECT id FROM visitor WHERE key=?;', (self.key,))
        visitor_id = cursor.fetchone()
        if not visitor_id:
            cursor.execute('''INSERT INTO visitor (userid, key, date_first_seen) VALUES (?,?,datetime(?));''', (self.user_id, self.key, self.date))
            visitor_id = cursor.lastrowid
            entry_processor.nb_new_visitor += 1
        else:
            visitor_id = visitor_id[0]
            if self.user_id != -1:
                cursor.execute('''UPDATE visitor SET userid=? WHERE id=?''', (self.user_id, visitor_id))
            
        # create user agent
        if self.user_agent in entry_processor.ua_data:
            useragent_id = entry_processor.ua_data[self.user_agent]
        else:
            cursor.execute('SELECT id FROM useragent WHERE useragent=?;', (self.user_agent,))
            useragent_id = cursor.fetchone()
            if not useragent_id:
                parsed_ua = uas_parser.parse(self.user_agent or ' ')
                cursor.execute('''INSERT INTO useragent (ua_name, os_company, os_name,ua_family,ua_company,os_url,typ,ua_company_url,ua_url,os_family,os_company_url, useragent)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?);''', (
                                              parsed_ua['ua_name'],
                                              parsed_ua['os_company'],
                                              parsed_ua['os_name'],
                                              parsed_ua['ua_family'],
                                              parsed_ua['ua_company'],
                                              parsed_ua['os_url'],
                                              parsed_ua['typ'],
                                              parsed_ua['ua_company_url'],
                                              parsed_ua['ua_url'],
                                              parsed_ua['os_family'],
                                              parsed_ua['os_company_url'],
                                              self.user_agent
                                              ))
                useragent_id = cursor.lastrowid
                entry_processor.nb_new_useragent += 1
            else:
                useragent_id = useragent_id[0]
            entry_processor.ua_data[self.user_agent] = useragent_id
        
        
        referer_id = self._get_or_create_referer(cursor)
        if self.http_referer.startswith(('http://elveos.org', 'https://elveos.org', 'http://www.elveos.org', 'https://www.elveos.org', 'https://mercanet.bnpparibas.net')):
            cursor.execute('''SELECT max(id) FROM visit WHERE id_visitor=?''', (visitor_id,))
            visit_id = cursor.fetchone()
            if visit_id:
                visit_id = visit_id[0]
            if not visit_id:
                visit_id = self._create_visit(cursor, visitor_id, useragent_id, referer_id)
            
        elif self.http_referer:
            visit_id = self._create_visit(cursor, visitor_id, useragent_id, referer_id)
        else:
            #if no referer, try an old one with the same user 
            cursor.execute(''' SELECT visit.id FROM visit  WHERE id_visitor=?
                               AND end_date >= datetime(?, '-30 minutes')''', (visitor_id,self.date))
            visit_id = cursor.fetchone()
            if not visit_id:
                cursor.execute('''SELECT id_visit FROM request WHERE remote_addr=? 
                                  AND date >= datetime(?, '-30 minutes')''', (self.remote_addr,self.date))
                visit_id = cursor.fetchone()
                if not visit_id:
                    visit_id = self._create_visit(cursor, visitor_id, useragent_id, referer_id)
                else:
                    visit_id = visit_id[0]
                    cursor.execute('''UPDATE visit SET end_date=? where id=?''', (self.date, visit_id))
                    
            else:
                visit_id = visit_id[0]
                cursor.execute('''UPDATE visit SET end_date=? where id=?''', (self.date, visit_id))
            
        if not visit_id:
            print visitor_id
            print visit_id

        #print 'Creating Request'
        cursor.execute('''INSERT INTO request 
            (id_linkable, id_visit, id_referer, locale, request_method, remote_addr, server_protocol, server_addr, accepted_languages, date, key, url)
            VALUES (?,    ?,        ?,          ?,      ?,              ?,           ?,               ?,           ?,               datetime(?),?,?)''',
            (linkable_id, visit_id, referer_id, self.lang, self.request_method, self.remote_addr, self.server_protocol, self.server_addr, self.accept_languages, self.date, self.key, self.request_uri))
        entry_processor.nb_new_request += 1
        

    def _get_or_create_referer(self, cursor):
        cursor.execute('SELECT id FROM externalurl WHERE external_url=?', (self.toAscii(self.http_referer),))
        referer_id = cursor.fetchone()
        if not referer_id:
            url = urlparse(self.toAscii(self.http_referer))
            cursor.execute('''INSERT INTO externalurl (scheme, netloc, path, params, query, fragment, external_url)
                VALUES (?,?,?,?,?,?,?)''', (url.scheme, url.netloc, url.path, url.params, url.query, url.fragment, self.toAscii(self.http_referer)))
            referer_id = cursor.lastrowid
            entry_processor.nb_new_referer += 1
        else:
            referer_id = referer_id[0]
            
        return referer_id
    
    def _create_visit(self, cursor, visitor_id, useragent_id, referer_id):
        #create the new visit
        cursor.execute('''INSERT INTO visit 
            (id_visitor, id_useragent, id_externalurl, begin_date, end_date, duration, real)
            VALUES (?, ?, ?, datetime(?), datetime(?), '0', '0')''', (visitor_id, useragent_id, referer_id, self.date, self.date))
        entry_processor.nb_new_visit += 1
        return cursor.lastrowid
        
    def toAscii(self, string):
        return string.decode("utf-8").encode("ascii", "ignore")
