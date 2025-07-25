package schneiderlab.tools.radialprojection.imageprocessor.core.convertczitotif;

public enum RotateDirection {
    DEGREES90_LEFT("90 Degrees Left"),
    DEGREES90_RIGHT("90 Degrees Right");

    private final String label;

    RotateDirection(String label) {
        this.label=label;
    }

    public String getLabel(){
        return label;
    }

    public static RotateDirection fromLabel(String s){
        for(RotateDirection c : values()){
            if(c.label.equalsIgnoreCase(s)) return c;
        }
        return null; // or throw exception
    }
}
