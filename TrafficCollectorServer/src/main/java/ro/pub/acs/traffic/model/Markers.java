package ro.pub.acs.traffic.model;

import java.util.List;
import javax.xml.bind.annotation.*;

@XmlRootElement(name = "marker")
public class Markers {

	private List<Marker> marker;

	public List<Marker> getMarker() {
		return marker;
	}

	@XmlElement
	public void setMarker(List<Marker> marker) {
		this.marker = marker;
	}

}
