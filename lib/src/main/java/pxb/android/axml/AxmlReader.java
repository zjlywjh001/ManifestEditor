/*
 * Copyright (c) 2009-2013 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pxb.android.axml;

import com.wind.meditor.property.ModificationProperty;
import com.wind.meditor.utils.NodeValue;
import com.wind.meditor.visitor.ApplicationTagVisitor;
import org.apache.commons.text.RandomStringGenerator;

import static pxb.android.axml.AxmlParser.END_FILE;
import static pxb.android.axml.AxmlParser.END_NS;
import static pxb.android.axml.AxmlParser.END_TAG;
import static pxb.android.axml.AxmlParser.START_FILE;
import static pxb.android.axml.AxmlParser.START_NS;
import static pxb.android.axml.AxmlParser.START_TAG;
import static pxb.android.axml.AxmlParser.TEXT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * a class to read android axml
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class AxmlReader {
	public static final NodeVisitor EMPTY_VISITOR = new NodeVisitor() {

		@Override
		public NodeVisitor child(String ns, String name) {
			return this;
		}

	};
	final AxmlParser parser;

	private List<ModificationProperty.MetaData> deleteMetaDataList = null;

	private List<ModificationProperty.MetaData> metaDataList;

	private boolean coexist = false;

	private String oldPackageName = null;

	private String newPackageName = null;

	private String providerSuffix = null;

	public AxmlReader(byte[] data) {
		super();
		this.parser = new AxmlParser(data);
		this.coexist = false;
	}

	public void accept(final AxmlVisitor av) throws IOException {
		Stack<NodeVisitor> nvs = new Stack<NodeVisitor>();
		NodeVisitor tos = av;
		while (true) {
			int type = parser.next();
			switch (type) {
			case START_FILE:
				break;
			case START_TAG:
				nvs.push(tos);
				boolean isdeleted = false;
				if (parser.getName().equals(NodeValue.MetaData.TAG_NAME) && (this.deleteMetaDataList != null)) {
					for (int i = 0; i < parser.getAttrCount(); i++) {
						if (parser.getAttrName(i).equals("name")) {
							for (ModificationProperty.MetaData dmd:this.deleteMetaDataList) {
								if (dmd.getName().equals(parser.getAttrValue(i))) {
									isdeleted = true;
								}
							}
						}
					}
				}
				if (isdeleted) {
					tos = EMPTY_VISITOR;
				}
				else {
					tos = tos.child(parser.getNamespaceUri(), parser.getName());
				}
				if (tos != null) {
					if (tos != EMPTY_VISITOR) {
						tos.line(parser.getLineNumber());
						ModificationProperty.MetaData modifyMata = null;
						for (int i = 0; i < parser.getAttrCount(); i++) {
							if (this.coexist) {
//								if (parser.getName().equals("manifest") && parser.getAttrName(i).equals("package")) {
//									System.out.println("Set "+parser.getAttrName(i)+"="+this.newPackageName);
//									tos.attr(parser.getAttrNs(i), parser.getAttrName(i), parser.getAttrResId(i),
//											parser.getAttrType(i), this.newPackageName);
//								}
								if (parser.getName().equals("permission") && parser.getAttrName(i).equals("name")) {
									if (((String)parser.getAttrValue(i)).contains(this.oldPackageName)) {
										String newperm = ((String)parser.getAttrValue(i)).replace(this.oldPackageName,this.newPackageName);
										tos.attr(parser.getAttrNs(i), parser.getAttrName(i), parser.getAttrResId(i),
												parser.getAttrType(i), newperm);
										//System.out.println("Set permission: "+parser.getAttrName(i)+"="+newperm);
									}

								}
								if (parser.getName().equals("uses-permission") && parser.getAttrName(i).equals("name")) {
									if (((String)parser.getAttrValue(i)).contains(this.oldPackageName)) {
										String newperm = ((String)parser.getAttrValue(i)).replace(this.oldPackageName,this.newPackageName);
										tos.attr(parser.getAttrNs(i), parser.getAttrName(i), parser.getAttrResId(i),
												parser.getAttrType(i), newperm);
										//System.out.println("Set uses-permission: "+parser.getAttrName(i)+"="+newperm);
									}

								}
								if (parser.getName().equals("provider") && parser.getAttrName(i).equals("authorities")) {
									if (this.providerSuffix == null) {
										char [][] pairs = {{'a','z'},{'0','9'},{'A','Z'}};
										this.providerSuffix = new RandomStringGenerator.Builder().withinRange(pairs).build().generate(3);

									}
									String newperm = ((String)parser.getAttrValue(i)).concat("."+this.providerSuffix);
									tos.attr(parser.getAttrNs(i), parser.getAttrName(i), parser.getAttrResId(i),
											parser.getAttrType(i), newperm);
									//System.out.println("Set provider: "+parser.getAttrName(i)+"="+newperm);
								}
							}
							if (parser.getName().equals(NodeValue.MetaData.TAG_NAME) && (this.metaDataList!=null)) {
								for (ModificationProperty.MetaData mmd:this.metaDataList) {
									if (("name".equals(parser.getAttrName(i))) && (mmd.getName().equals(parser.getAttrValue(i)))) {
										//System.out.println("Existing meta-data name = "+mmd.getName());
										ApplicationTagVisitor.AddExistingMetaName(mmd.getName());
										tos.attr(parser.getAttrNs(i), parser.getAttrName(i), parser.getAttrResId(i),
												parser.getAttrType(i), parser.getAttrValue(i));
										modifyMata = mmd;
										break;
									}

								}

								if ((modifyMata!=null) && "value".equals(parser.getAttrName(i))) {
									//System.out.println("Modify meta-data Vlaue = "+modifyMata.getValue());
									tos.attr(parser.getAttrNs(i), parser.getAttrName(i), parser.getAttrResId(i),
											parser.getAttrType(i), modifyMata.getValue());
								} else {
										tos.attr(parser.getAttrNs(i), parser.getAttrName(i), parser.getAttrResId(i),
												parser.getAttrType(i), parser.getAttrValue(i));
								}
							} else {
								tos.attr(parser.getAttrNs(i), parser.getAttrName(i), parser.getAttrResId(i),
										parser.getAttrType(i), parser.getAttrValue(i));
							}
						}
					}
				} else {
					tos = EMPTY_VISITOR;
				}
				break;
			case END_TAG:
				tos.end();
				tos = nvs.pop();
				break;
			case START_NS:
				av.ns(parser.getNamespacePrefix(), parser.getNamespaceUri(), parser.getLineNumber());
				break;
			case END_NS:
				break;
			case TEXT:
				tos.text(parser.getLineNumber(), parser.getText());
				break;
			case END_FILE:
				return;
			default:
				System.err.println("AxmlReader: Unsupported tag: " + type);
			}
		}
	}

	public void setMetaDataList(List<ModificationProperty.MetaData> metalist) {
		this.metaDataList = metalist;
	}
	public void setDeleteMetaDataList(List<ModificationProperty.MetaData> dellist) {
		this.deleteMetaDataList = dellist;
	}

	public void setPackageNamePair(String oldN, String newN) {
		this.oldPackageName = oldN;
		this.newPackageName = newN;

	}

	public void set_coexist(boolean on) {
		this.coexist = on;
	}
}
