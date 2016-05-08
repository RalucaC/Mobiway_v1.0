package ro.pub.acs.traffic.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Table(name = "journey_data")
public class JourneyData implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "id")
	private Integer id;

	@Column(name = "latitude", nullable=true)
	private Float latitude;

	@Column(name = "longitude", nullable=true)
	private Float longitude;

	@Column(name = "speed", nullable=true)
	private Integer speed;

	@Basic(optional = false)
	@NotNull
	@Column(name = "timestamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;
	
	@Lob
	@Column(name = "osm_way_id", nullable=true)
	private String osmWayId;

	@JoinColumn(name = "journey_id", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private Journey journeyId;

	public JourneyData() {
	}

	public JourneyData(Integer id) {
		this.id = id;
	}

	public JourneyData(Integer id, Float latitude, Float longitude, Integer speed,
			Date timestamp) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.speed = speed;
		this.timestamp = timestamp;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Float getLatitude() {
		return latitude;
	}

	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

	public Float getLongitude() {
		return longitude;
	}

	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}

	public Integer getSpeed() {
		return speed;
	}

	public void setSpeed(Integer speed) {
		this.speed = speed;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Journey getJourneyId() {
		return journeyId;
	}
	
	public String getOsmWayId() {
		return osmWayId;
	}
	
	public void setOsmWayId(String osmWayId) {
		this.osmWayId = osmWayId;
	}

	public void setJourneyId(Journey journeyId) {
		this.journeyId = journeyId;
	}

	@Override
	public String toString() {
		return "ro.pub.acs.traffic.model.JourneyData[ id=" + id
				+ " ]";
	}

}
