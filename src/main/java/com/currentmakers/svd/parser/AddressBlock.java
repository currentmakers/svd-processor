package com.currentmakers.svd.parser;

import org.w3c.dom.Element;

public class AddressBlock
{
    public int offset;
    public int size;
    public Usage usage;

    public AddressBlock(Element addressBlockElement)
    {
        offset = Integer.parseInt(addressBlockElement.getElementsByTagName("offset").item(0).getTextContent());
        size = Integer.parseInt(addressBlockElement.getElementsByTagName("size").item(0).getTextContent());
        usage = Usage.valueOf((addressBlockElement.getElementsByTagName("usage").item(0).getTextContent()));
    }
}
