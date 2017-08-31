package ro.pub.acs.mobiway.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "street_mean_speeds")
public class StreetMeanSpeed implements Serializable {

    private static final long serialVersionUID = 1L;

    public StreetMeanSpeed() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "street_name")
    private String streetName;

    @Column(name = "mean_speed")
    private Float meanSpeed;

    @Column(name = "number_of_measures")
    private Integer numberOfMeasures;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public Float getMeanSpeed() {
        return meanSpeed;
    }

    public void setMeanSpeed(Float meanSpeed) {
        this.meanSpeed = meanSpeed;
    }

    public Integer getNumberOfMeasures() {
        return numberOfMeasures;
    }

    public void setNumberOfMeasures(Integer numberOfMeasures) {
        this.numberOfMeasures = numberOfMeasures;
    }

    public String toString() {
        return streetName + " " + meanSpeed + " km/h";
    }
}
