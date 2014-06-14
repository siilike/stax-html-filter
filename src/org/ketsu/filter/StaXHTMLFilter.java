package org.ketsu.filter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
public class StaXHTMLFilter extends StaXFilter
{
	private static final StaXHTMLFilter defaultInstance = new StaXHTMLFilter();

	protected final Map<String, Set<String>> allowedTags = new ConcurrentHashMap<String, Set<String>>(50, 0.9f, 1);
	protected final Set<String> selfClosingTags = new HashSet<String>();
	protected final Set<String> protocolAttrs = new HashSet<String>();
	protected final Set<String> allowedProtocols = new HashSet<String>();
	protected final Set<String> allowedIframeSources = new HashSet<String>();
	protected final Set<String> ignoreBlanks = new HashSet<String>();

	public StaXHTMLFilter()
	{
		configure();
	}

	public static StaXHTMLFilter getDefaultInstance()
	{
		return defaultInstance;
	}

	@Override
	public String getFakeRoot()
	{
		return "html";
	}

	protected void configure()
	{
		configureAllowedTags();
		configureSelfClosingTags();
		configureProtocolAttrs();
		configureAllowedProtocols();
		configureAllowedIframeSources();
		configureIgnoreBlanks();
	}

	public void configureAllowedTags()
	{
		Set<String> noAttrs = new HashSet<String>();

		Set<String> styleAttrs = new HashSet<String>();
		styleAttrs.add("style");

		Set<String> aAttrs = new HashSet<String>();
		aAttrs.add("href");

		Set<String> tableAttrs = new HashSet<String>();
		tableAttrs.add("cellpadding");
		tableAttrs.add("cellspacing");
		tableAttrs.add("style");

		Set<String> imgAttrs = new HashSet<String>();
		imgAttrs.add("src");
		imgAttrs.add("width");
		imgAttrs.add("height");
		imgAttrs.add("alt");
		imgAttrs.add("style");

		Set<String> divAttrs = new HashSet<String>();
		divAttrs.add("width");
		divAttrs.add("height");
		divAttrs.add("style");

		Set<String> objectAttrs = new HashSet<String>();
		objectAttrs.add("width");
		objectAttrs.add("height");
		objectAttrs.add("style");
		objectAttrs.add("data");
		objectAttrs.add("type");

		Set<String> iframeAttrs = new HashSet<String>();
		iframeAttrs.add("width");
		iframeAttrs.add("height");
		iframeAttrs.add("style");
		iframeAttrs.add("src");

		allowedTags.put("h1", noAttrs);
		allowedTags.put("h2", noAttrs);
		allowedTags.put("h3", noAttrs);
		allowedTags.put("h4", noAttrs);
		allowedTags.put("h5", noAttrs);
		allowedTags.put("h6", noAttrs);
		allowedTags.put("details", noAttrs);
		allowedTags.put("summary", noAttrs);

		allowedTags.put("table", tableAttrs);
		allowedTags.put("td", noAttrs);
		allowedTags.put("th", noAttrs);
		allowedTags.put("tr", noAttrs);
		allowedTags.put("tbody", noAttrs);

		allowedTags.put("a", aAttrs);
		allowedTags.put("img", imgAttrs);
		allowedTags.put("b", noAttrs);
		allowedTags.put("strong", noAttrs);
		allowedTags.put("i", noAttrs);
		allowedTags.put("em", noAttrs);
		allowedTags.put("cite", noAttrs);
		allowedTags.put("blockquote", noAttrs);
		allowedTags.put("abbr", noAttrs);
		allowedTags.put("acronym", noAttrs);
		allowedTags.put("sub", noAttrs);
		allowedTags.put("sup", noAttrs);
		allowedTags.put("pre", noAttrs);
		allowedTags.put("address", noAttrs);

		allowedTags.put("object", objectAttrs);
		allowedTags.put("iframe", iframeAttrs);

		allowedTags.put("span", styleAttrs);
		allowedTags.put("div", divAttrs);
		allowedTags.put("p", styleAttrs);

		allowedTags.put("ul", styleAttrs);
		allowedTags.put("ol", styleAttrs);
		allowedTags.put("li", noAttrs);

		allowedTags.put("br", noAttrs);
	}

	protected void configureIgnoreBlanks()
	{
		ignoreBlanks.add("p");
		ignoreBlanks.add("a");

		ignoreBlanks.add("b");
		ignoreBlanks.add("i");
		ignoreBlanks.add("u");
		ignoreBlanks.add("em");
		ignoreBlanks.add("strong");

		ignoreBlanks.add("img");
	}

	protected void configureAllowedProtocols()
	{
		allowedProtocols.add("http");
		allowedProtocols.add("https");
		allowedProtocols.add("mailto");
	}

	protected void configureProtocolAttrs()
	{
		protocolAttrs.add("src");
		protocolAttrs.add("rel");
	}

	protected void configureAllowedIframeSources()
	{
		allowedIframeSources.add("http://www.youtube.com/embed/");
		allowedIframeSources.add("https://www.youtube.com/embed/");

		allowedIframeSources.add("http://player.vimeo.com/video/");
		allowedIframeSources.add("https://player.vimeo.com/video/");
	}

	protected void configureSelfClosingTags()
	{
		selfClosingTags.add("img");
		selfClosingTags.add("br");
	}

	@Override
	public boolean isAllowedAttribute(String tag, String attr, String val)
	{
		if(protocolAttrs.contains(attr))
		{
			if(tag.equals("iframe"))
			{
				for(String v : allowedIframeSources)
				{
					if(val.startsWith(v))
					{
						return true;
					}
				}

				return false;
			}

			if(tag.equals("img") && val.startsWith("data:"))
			{
				return true;
			}

			for(String a : allowedProtocols)
			{
				if(val.startsWith(a))
				{
					return true;
				}
			}

			return false;
		}

		return allowedTags.get(tag).contains(attr);
	}

	@Override
	public boolean isAllowedAttribute(String tag, String prefix, String ns, String attr, String val)
	{
		return false;
	}

	@Override
	public boolean isAllowedTag(String tag)
	{
		return allowedTags.containsKey(tag);
	}

	@Override
	public boolean isIgnoreBlankTag(String tag)
	{
		return ignoreBlanks.contains(tag);
	}

	@Override
	public boolean isSelfClosingElement(String tag)
	{
		return selfClosingTags.contains(tag);
	}
}
