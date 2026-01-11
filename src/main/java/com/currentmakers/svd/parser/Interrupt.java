package com.currentmakers.svd.parser;

import org.w3c.dom.Element;

public class Interrupt
{
    public String name;
    public String description;
    public int value;

    public Interrupt(Element interruptElement)
    {
        name = interruptElement.getElementsByTagName("name").item(0).getTextContent();
        description = interruptElement.getElementsByTagName("description").item(0).getTextContent();
        value = Integer.parseInt(interruptElement.getElementsByTagName("value").item(0).getTextContent());
    }
}
