package com.here.android.tutorial;

public class Position {

    Double x;
    Double y;
    String name;
    String typ;

    Boolean prywatne;
    Boolean platne;
    Boolean darmowe;


    //Setters and Getters
    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public Boolean getPrywatne() {
        return prywatne;
    }

    public void setPrywatne(Boolean prywatne) {
        this.prywatne = prywatne;
    }

    public Boolean getPlatne() {
        return platne;
    }

    public void setPlatne(Boolean platne) {
        this.platne = platne;
    }

    public Boolean getDarmowe() {
        return darmowe;
    }

    public void setDarmowe(Boolean darmowe) {
        this.darmowe = darmowe;
    }
}
