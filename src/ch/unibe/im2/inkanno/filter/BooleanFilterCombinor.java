/**
 * 
 */
package ch.unibe.im2.inkanno.filter;

import ch.unibe.eindermu.utils.NotImplementedException;
import ch.unibe.inkml.InkTraceView;
import ch.unibe.inkml.util.AbstractTraceFilter;
import ch.unibe.inkml.util.TraceViewFilter;

/**
 * @author emanuel
 *
 */
public class BooleanFilterCombinor extends AbstractTraceFilter {
    
    private TraceViewFilter a,b;
    private Type type;
    public enum Type{
        AND,
        OR,
        XOR
    };
    
    public BooleanFilterCombinor(Type type, TraceViewFilter a, TraceViewFilter b){
        this.a = a;
        this.b = b;
        this.type = type;
    }
    
    @Override
    public boolean pass(InkTraceView view) {
        switch(type){
        case AND:
            return a.pass(view) && b.pass(view);
        case OR:
            return a.pass(view) || b.pass(view);
        case XOR:
            boolean ba = a.pass(view),
            bb = b.pass(view);
            return (ba || bb) && (!ba || !bb);
        }
        throw new NotImplementedException();
    }

}
