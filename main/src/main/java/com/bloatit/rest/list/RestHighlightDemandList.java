package com.bloatit.rest.list;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.bloatit.framework.utils.PageIterable;
import com.bloatit.model.HighlightDemand;
import com.bloatit.rest.list.master.RestListBinder;
import com.bloatit.rest.resources.RestHighlightDemand;

@XmlRootElement
public class RestHighlightDemandList extends RestListBinder<RestHighlightDemand, HighlightDemand> {
    public RestHighlightDemandList(PageIterable<HighlightDemand> collection) {
        super(collection);
    }
    
    @XmlElementWrapper(name = "highlightdemands")
    @XmlElement(name = "highlightdemand")
    public RestHighlightDemandList getHighlightDemands() {
        return this;
    }
}

