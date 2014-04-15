package jp.sblo.pandora.aGrep;

public class CheckedString {
    boolean checked;
    String string;

    public CheckedString(String _s){
        this(true,_s);
    }
    public CheckedString(boolean _c,String _s){
        checked = _c;
        string = _s;
    }
    public String toString(){
        return (checked?"true":"false") + "|" + string;
    }

}