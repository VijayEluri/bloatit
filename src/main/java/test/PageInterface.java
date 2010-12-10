package test;

import com.bloatit.web.utils.RequestParamSetter.Messages;
import test.html.HtmlElement;

public interface PageInterface {

    public HtmlElement create();
    
    public String getName();
    
    public String getCustomCss();
    
    public void addNotifications(Messages messages);
    
}