/*
 * Copyright (C) 2010 BloatIt.
 *
 * This file is part of BloatIt.
 *
 * BloatIt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BloatIt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */

package com.bloatit.web.pages;

import com.bloatit.framework.MemberManager;
import com.bloatit.model.Member;
import com.bloatit.model.exceptions.ElementNotFoundException;
import com.bloatit.web.htmlrenderer.htmlcomponent.HtmlComponent;
import com.bloatit.web.htmlrenderer.htmlcomponent.HtmlString;
import com.bloatit.web.htmlrenderer.htmlcomponent.HtmlText;
import com.bloatit.web.htmlrenderer.htmlcomponent.HtmlTitle;
import com.bloatit.web.server.Page;
import com.bloatit.web.server.Session;
import java.util.HashMap;
import java.util.Map;


public class MemberPage extends Page {

    private final Member member;
    

    public MemberPage(Session session, Map<String, String> parameters) {
        this(session, parameters, null);
    }

    public MemberPage(Session session, Member member) {
        this(session, new HashMap<String, String>(), member);
    }

    public MemberPage(Session session, Map<String, String> parameters, Member member) {
        super(session, parameters);
        Member d = null;

        if (member == null) {
            if (parameters.containsKey("id")) {
                Integer id = null;
                try{
                    id = new Integer(parameters.get("id"));
                } catch(NumberFormatException e){

                }
                if (id != null) {
                    try {
                        d = MemberManager.GetMemberById(id);
                    } catch (ElementNotFoundException ex) {
                    }
                }
            }
        } else {
            d = member;
        }
        this.member = d;
    }


    @Override
    protected HtmlComponent generateContent() {
        if (this.member != null) {
          
            HtmlTitle memberTitle = new HtmlTitle(member.getFullName(), "");
            
            memberTitle.add(new HtmlText("Full name: " + member.getFullName()));
            memberTitle.add(new HtmlText("Login: " + member.getLogin()));
            memberTitle.add(new HtmlText("Email: " + member.getEmail()));
            memberTitle.add(new HtmlText("Karma: " + member.getKarma()));

            return memberTitle;
        } else {
            return new HtmlTitle("No member", "");
        }
    }

    @Override
    public String getCode() {
        if (this.member != null) {
            return new HtmlString(session).add("member/id-" + this.member.getId() + "/title-").secure(member.getLogin()).toString();
        } else {
            return "member"; // TODO Faire un système pour afficher une page d'erreur
        }
    }

    @Override
    protected String getTitle() {
        if (this.member != null) {
            return "Member - " + member.getLogin();
        } else {
            return "Member - No member";
        }
    }

}