package com.currentmakers.svd.parser;

import org.w3c.dom.Element;

import static com.currentmakers.svd.parser.Util.getNumber;

public class AddressBlock
{
    public int offset;
    public int size;
    public Usage usage;

    @SuppressWarnings("DataFlowIssue")
    public AddressBlock(Element addressBlockElement)
    {
        offset = getNumber(addressBlockElement,"offset").intValue();
        size = getNumber(addressBlockElement,"size").intValue();
        usage = Usage.valueOf((addressBlockElement.getElementsByTagName("usage").item(0).getTextContent()));
    }
}
