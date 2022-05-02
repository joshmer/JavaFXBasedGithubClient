public class Extras {
    String propertyName;

    Extras(String propertyName) {
        this.propertyName = propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String toString() {
        return this.propertyName;
    }
}
