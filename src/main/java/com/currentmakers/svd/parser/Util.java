package com.currentmakers.svd.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.function.Consumer;

public class Util
{
    static boolean getBoolean(Element element, String tag)
    {
        NodeList list = element.getElementsByTagName(tag);
        Node item = list.item(0);
        if(item == null)
            return false;
        return Boolean.parseBoolean(item.getTextContent());
    }

    static String getText(Element element, String tag)
    {
        NodeList list = element.getElementsByTagName(tag);
        Node item = list.item(0);
        if(item == null)
            return null;
        String textContent = item.getTextContent();
        textContent = textContent.trim()
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\t', ' ')
        ;
        return textContent;
    }

    static long getNumber(Element element, String tag)
    {
        NodeList list = element.getElementsByTagName(tag);
        Node item = list.item(0);
        if(item == null)
            return 0;
        String text = item.getTextContent();
        if(text.startsWith("0x") || text.startsWith("0X"))
            return Long.parseLong(text.substring(2), 16);
        if(text.startsWith("0b") || text.startsWith("0B"))
            return Long.parseLong(text.substring(2), 2);
        return Long.parseLong(text);
    }

    static void forEach(Element root, String parent, String children, Consumer<Element> target)
    {
        Element parentElement = (Element) root.getElementsByTagName(parent).item(0);
        if(parentElement == null)
            return;
        NodeList childrenList = parentElement.getElementsByTagName(children);
        for(int i = 0; i < childrenList.getLength(); i++)
        {
            target.accept((Element) childrenList.item(i));
        }

    }
}
