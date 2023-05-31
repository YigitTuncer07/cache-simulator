public class Line {
    byte validBit;
    String tag;
    String data;

    public Line (){
        tag = "";
        data = "";
        validBit = 0;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public byte getValidBit() {
        return validBit;
    }

    public void setValidBit(byte validBit) {
        this.validBit = validBit;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        System.out.println("Data used to be " + this.data + " || is now " + data);
        this.data = data;
    }

    public String toString(){
        if (tag == ""){
            return "------- " + validBit + " " + "--------";
        }
        return tag + " " + validBit + " " + data;
    }
}
