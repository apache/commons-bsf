package org.apache.bsf.e4x;

import org.apache.axiom.om.OMNode;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextHelper;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.xml.XMLObject;
import org.mozilla.javascript.xmlimpl.XML;

public class E4XHelper {

	private ScriptableObject scope;

	public E4XHelper() {
        Context cx = Context.enter();
        try {

            this.scope = cx.initStandardObjects();

        } finally {
            Context.exit();
        }
    }

	public XMLObject toE4X(OMNode om) {
        Context cx = Context.enter();
        try {
        	ContextHelper.setTopCallScope(cx, scope);

            return (XML)cx.newObject(scope, "XML", new Object[] {om});

        } finally {
        	ContextHelper.setTopCallScope(cx, null);
            Context.exit();
        }
    }

    public OMNode fromE4X(XMLObject xml) {
        return ((XML)xml).getAxiomFromXML();
    }

}
