StaX-based (HTML) filter
========================

Allows you to configure allowed tags, attributes, protocols, etc.

All input has to be valid XML, so you might want to preprocess it to ensure it is semantically valid. As XML documents are required to have only one root element, a fake root element is added for processing time to ensure that.


Usage example
-------------

    StaxHTMLFilter filter = StaxHTMLFilter.getDefaultInstance();
    String filtered = filter.filter(userInput);
