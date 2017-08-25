package ro.pub.acs.mobiway.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.scheduling.TaskScheduler;
import ro.pub.acs.mobiway.dao.StreetMeanSpeedDAO;
import ro.pub.acs.mobiway.model.StreetMeanSpeed;
import ro.pub.acs.mobiway.model.StreetSpeeds;
import ro.pub.acs.mobiway.utils.Constants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static ro.pub.acs.mobiway.utils.Constants.FACEBOOK_PAGE_ID;

public class PostTrafficStatusToFacebook {

    private TaskScheduler taskScheduler;
    private StreetMeanSpeedDAO streetMeanSpeedDAO;
    private StreetSpeeds streetSpeeds;

    private class PostTrafficStatusToFacebookTask implements Runnable {

        @Override
        public void run() {
            List<StreetMeanSpeed> streetMeanSpeeds = streetMeanSpeedDAO.getAllStreetMeanSpeeds();

            Collections.sort(streetMeanSpeeds, new Comparator<StreetMeanSpeed>() {
                @Override
                public int compare(StreetMeanSpeed o1, StreetMeanSpeed o2) {
                    if (o1.getMeanSpeed() - o2.getMeanSpeed() > 0) {
                        return -1;
                    } else if (o1.getMeanSpeed() - o2.getMeanSpeed() < 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            String clasament = "Topul strazilor dupa viteza medie\n\n";
            int index = 1;
            for (StreetMeanSpeed streetMeanSpeed : streetMeanSpeeds) {
                clasament += index + ". " + streetMeanSpeed.toString() + "\n";
                index++;
            }

            HttpClient httpClient = new DefaultHttpClient();

            StringBuilder url = new StringBuilder();

            url.append("https://graph.facebook.com/");
            url.append(FACEBOOK_PAGE_ID);
            url.append("/feed?");

            try {
                url.append("message=" + URLEncoder.encode(clasament, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            url.append("&access_token=");
            url.append(Constants.MOBIWAY_PAGE_ACCESS_TOKEN);

            HttpPost httpPost = new HttpPost(url.toString());

            HttpResponse httpPostResponse = null;
            try {
                httpPostResponse = httpClient.execute(httpPost);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            streetSpeeds.resetStreetSpeeds();
            streetMeanSpeedDAO.deleteAllRecords();
        }
    }

    public PostTrafficStatusToFacebook(TaskScheduler taskScheduler, StreetSpeeds streetSpeeds, StreetMeanSpeedDAO streetMeanSpeedDAO) {
        this.taskScheduler = taskScheduler;
        this.streetSpeeds = streetSpeeds;
        this.streetMeanSpeedDAO = streetMeanSpeedDAO;

        taskScheduler.scheduleAtFixedRate(new PostTrafficStatusToFacebook.PostTrafficStatusToFacebookTask(), new Date(new Date().getTime() + 5000),
                60 * 60 * 1000);
    }

}
