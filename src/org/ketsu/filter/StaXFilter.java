package org.ketsu.filter;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Lauri Keel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public abstract class StaXFilter
{
	public static final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	public static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

	public final String fakeRoot = getFakeRoot();

	/*
	 * FIXME: does not remove blank tags with (only forbidden) attributes
	 */
	public abstract boolean isIgnoreBlankTag(String tag);

	public abstract boolean isAllowedTag(String tag);
	public abstract boolean isSelfClosingElement(String tag);
	public abstract boolean isAllowedAttribute(String tag, String attr, String val);
	public abstract boolean isAllowedAttribute(String tag, String prefix, String ns, String attr, String val);

	public String getFakeRoot()
	{
		return null;
	}

	public String filter(String what) throws XMLStreamException
	{
		Writer output = new StringWriter(what.length() + (fakeRoot != null ? fakeRoot.length()*2 + 5 : 0));
		Reader input  = new StringReader(fakeRoot != null ? ("<"+fakeRoot+">"+what+"</"+fakeRoot+">") : what);

		XMLStreamWriter writer = outputFactory.createXMLStreamWriter(output);
		XMLStreamReader reader = inputFactory.createXMLStreamReader(input);

		filter(inputFactory, reader, writer);

		String ret = output.toString();

		if(fakeRoot != null)
		{
			int len = ret.length();
			int frl = fakeRoot.length()+2;

			return ret.substring(frl, len-frl-1);
		}

		return ret;
	}

	public void filter(XMLInputFactory inputFactory, XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException
	{
		reader = inputFactory.createFilteredReader(reader, new StreamFilter()
		{
	    private int ignoreDepth = 0;

	    @Override
			public boolean accept(XMLStreamReader reader)
	    {
	    	if(reader.isStartElement() || reader.isEndElement())
	    	{
	    		if(fakeRoot != null && reader.getLocalName().equals(fakeRoot))
	    		{
	    			return false;
	    		}

		    	if(!isAllowedTag(reader.getLocalName()))
		    	{
		    		if(reader.isStartElement())
		    		{
		    			ignoreDepth++;
		    			return false;
		    		}

		    		if(reader.isEndElement())
		    		{
		    			ignoreDepth--;
		    			return false;
		    		}
		    	}
	    	}

        return (ignoreDepth == 0);
	    }
		});

		if(fakeRoot != null)
		{
			writer.writeStartElement(fakeRoot);
		}

		String started = null;
		while(reader.hasNext())
		{
			int ev = reader.getEventType();

			if(started != null && ev != XMLStreamConstants.END_ELEMENT)
			{
				writer.writeStartElement(started);
				started = null;
			}

			if(ev == XMLStreamConstants.START_ELEMENT)
			{
				int attrs = reader.getAttributeCount();

				String tag = reader.getLocalName();

				if(attrs == 0 && isIgnoreBlankTag(tag))
				{
					started = tag;
				}
				else
				{
					writer.writeStartElement(tag);

					for(int i = 0; i < attrs; i++)
					{
						String ns = reader.getAttributeNamespace(i);
						String pf = reader.getAttributePrefix(i);

						if(pf == null || pf.isEmpty())
						{
							String an = reader.getAttributeLocalName(i);
							String av = reader.getAttributeValue(i);

							if(isAllowedAttribute(tag, an, av))
							{
								writer.writeAttribute(an, av);
							}
						}
						else
						{
							String an = reader.getAttributeLocalName(i);
							String av = reader.getAttributeValue(ns, an);

							if(isAllowedAttribute(tag, pf, ns, an, av))
							{
								writer.writeAttribute(pf, ns, an, av);
							}
						}
					}
				}
			}
			else if(ev == XMLStreamConstants.END_ELEMENT)
			{
				if(started == null)
				{
					if(!isSelfClosingElement(reader.getLocalName()))
					{
						writer.writeCharacters("");
					}

					writer.writeEndElement();
				}

				started = null;
			}
			else if(ev == XMLStreamConstants.CHARACTERS)
			{
				writer.writeCharacters(reader.getText());
			}

			reader.next();
		}

		if(fakeRoot != null)
		{
			writer.writeEndElement();
		}

		writer.writeEndDocument();

		reader.close();
		writer.close();
	}
}
