package com.example.admin.betty.Activity;


import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.betty.Info.GpsInfo;
import com.example.admin.betty.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.widget.Toast.LENGTH_SHORT;

public class HomeActivity extends Fragment {
    private FirebaseDatabase mscheduleDatabase;
    private DatabaseReference mscheduleReference;
    private DatabaseReference mlocationReference;
    private FirebaseAuth mAuth;
    private ArrayAdapter<String> Padapter, Sadapter;
    List<Object> pArray = new ArrayList<Object>();
    List<Object> sArray = new ArrayList<Object>();
    String Uid;

    TextView tmx;
    TextView tmn;
    TextView location;
    TextView temperature;
    TextView current;
    TextView temcloth;
    ImageButton btnLocation;
    ImageView weather_Icon = null;
    ListView preparelist, schedulelist;

    String weatherendpoint = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastSpaceData?";
    String forecastendpoint = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastTimeData?";
    String currentendpoint = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastGrib?";
    String key="dDw%2BIZmXJgVoq5s7ykMsHVW7Pbr%2FueK49QRY5HECqvCimV9k0gZUQdnP0GoMfBn%2FXafD3LpV3%2B8nqsQTtjpudg%3D%3D";

    String temperatureendpoint = "http://newsky2.kma.go.kr/iros/RetrieveLifeIndexService3/getSensorytemLifeList?";
    String temperaturekey = "dDw%2BIZmXJgVoq5s7ykMsHVW7Pbr%2FueK49QRY5HECqvCimV9k0gZUQdnP0GoMfBn%2FXafD3LpV3%2B8nqsQTtjpudg%3D%3D";

    //오늘 년월일, 현재시간
    Date date = new Date();
    SimpleDateFormat format_date = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
    String weather_date = format_date.format(date);
    SimpleDateFormat format_time = new SimpleDateFormat("kk", Locale.KOREA);
    String strhour = format_time.format(date);
    int hour = Integer.parseInt(strhour) - 1;
    String weather_time = String.format("%02d", hour) + "00";
    String temperature_time = String.format("%02d", hour);

    //동네예보 70줄 url
    String weather_url = weatherendpoint + "serviceKey=" + key + "&base_date=" + weather_date
            + "&base_time=0200&nx=60&ny=125&numOfRows=70&_type=xml";
    //단기예보 30줄 url
    String forecast_url = forecastendpoint + "serviceKey=" + key + "&base_date=" + weather_date + "&base_time=" + weather_time
            + "&nx=60&ny=125&numOfRows=30&_type=xml";
    //단기실황 8줄 url
    String current_url = currentendpoint + "serviceKey=" + key + "&base_date=" + weather_date + "&base_time=" + weather_time
            + "&nx=60&ny=125&numOfRows=8&_type=xml";
    //체감온도
    String temperature_url = temperatureendpoint + "serviceKey=" + temperaturekey + "&areaNo=1100000000&time=" + weather_date + temperature_time;

    //위경도<->좌표
    public static int TO_GRID = 0;
    public static int TO_GPS = 1;
    // GPS 권한
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    // GPSTracker class
    private GpsInfo gps;
    // GPS value
    double latitude, longitude = 0.0;

    //하루동안 강수 예보
    String[] rain_probabillity = new String[7];
    String[] rain = new String[7];
    int precipitation;
    int sky;
    boolean w_wind = false;
    boolean w_lightning = false;

    public HomeActivity() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_home, container, false);

        //TODO 여기에 View를 찾고 이벤트를 등록하고 등등의 처리를 할 수 있다.
        Calendar cal = Calendar.getInstance();
        int year = cal.get ( cal.YEAR );
        int month = cal.get ( cal.MONTH ) + 1 ;
        int date = cal.get ( cal.DATE ) ;
        String strcal = year + "" + month + "" + date;

        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();
        mscheduleDatabase = FirebaseDatabase.getInstance();
        mscheduleReference = mscheduleDatabase.getReference().child("schedules").child(Uid).child(strcal);
        mlocationReference = mscheduleDatabase.getReference().child("information").child(Uid).child("location");
        getuserLocation();
        //Toast.makeText(getActivity(), strcal, Toast.LENGTH_SHORT).show();

        tmn = (TextView)view.findViewById(R.id.tmn);
        tmx = (TextView)view.findViewById(R.id.tmx);
        current = (TextView)view.findViewById(R.id.current);
        temperature = (TextView)view.findViewById(R.id.temperature);
        weather_Icon = (ImageView)view.findViewById(R.id.weatherIcon);
        temcloth = (TextView)view.findViewById(R.id.temcloth);

        //GPS 위경도, 주소 구하기
        location = (TextView)view.findViewById(R.id.location);
        btnLocation = (ImageButton) view.findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 권한 요청을 해야 함
                if (!isPermission) {
                    callPermission();
                    return;
                }

                gps = new GpsInfo(getContext());
                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

                    getLocation(latitude, longitude);
                    LatXLngY grid = convertGRID_GPS(TO_GRID, latitude, longitude);
                    int x = (int)grid.x;
                    int y = (int)grid.y;

                    if (latitude == 0.0 && longitude == 0.0){
                        Toast.makeText(getActivity(), "위치를 찾을 수 없습니다", Toast.LENGTH_LONG).show();
                        getuserLocation();
                    } else {
                        String strlocation = location.getText().toString();

                        Toast.makeText(getActivity(), "현재 위치 - \n위도: " + latitude + "\n경도: " + longitude, Toast.LENGTH_LONG).show();
                        writeuserLocation(x, y, strlocation);
                    }

                    new GetXMLLocalWeather().execute();
                    new GetXMLforecastWeather().execute();
                    new GetXMLCurrentWeather().execute();
                    new GetXMLApparentTem().execute();
                    weatherIcon();
                } else {
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }
            }
        });
        callPermission();  // 권한 요청을 해야 함

        new GetXMLLocalWeather().execute();
        new GetXMLforecastWeather().execute();
        new GetXMLCurrentWeather().execute();
        new GetXMLApparentTem().execute();
        weatherIcon();

        preparelist = (ListView)view.findViewById(R.id.home_list);
        schedulelist = (ListView)view.findViewById(R.id.home_schedule_list);
        Padapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        Sadapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        preparelist.setAdapter(Padapter);
        schedulelist.setAdapter(Sadapter);

        mscheduleReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String title = snapshot.getKey();
                    if(title.isEmpty()) {
                    } else {
                        sArray.add(title);
                        Sadapter.add(title);
                    }

                    mscheduleReference.child(title).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ScheduleDTO schedule = dataSnapshot.getValue(ScheduleDTO.class);

                            if(schedule.prepare.isEmpty()){
                                //일정이 없음
                            } else {
                                pArray.add(schedule.prepare);
                                Padapter.add(schedule.prepare);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                if (precipitation != 0){
                    pArray.add("우산");
                    Padapter.add("우산");
                }

                Sadapter.notifyDataSetChanged();
                Padapter.notifyDataSetChanged();
                schedulelist.setSelection(Sadapter.getCount() - 1);
                preparelist.setSelection(Padapter.getCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessFineLocation = true;
        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    //파이어베이스에서 위치값 가져오기
    private void getuserLocation(){
        mlocationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LocationDTO locationDTO = dataSnapshot.getValue(LocationDTO.class);
                int x = locationDTO.userX;
                int y = locationDTO.userY;

                //동네예보 70줄 url
                weather_url = weatherendpoint + "serviceKey=" + key + "&base_date=" + weather_date + "&base_time=0200"
                        + "&nx=" + x + "&ny=" + y + "&numOfRows=70&_type=xml";
                //단기예보 30줄 url
                forecast_url = forecastendpoint + "serviceKey=" + key + "&base_date=" + weather_date + "&base_time=" + weather_time
                        + "&nx=" + x + "&ny=" + y + "&numOfRows=30&_type=xml";
                //단기실황 8줄 url
                current_url = currentendpoint + "serviceKey=" + key + "&base_date=" + weather_date + "&base_time=" + weather_time
                        + "&nx=" + x + "&ny=" + y + "&numOfRows=8&_type=xml";

                LatXLngY gps = convertGRID_GPS(TO_GPS, x, y);
                double lat = gps.lat;
                double lng = gps.lng;
                getLocation(lat, lng);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //파이어베이스에 위치값 저장
    private void writeuserLocation(int x, int y, String strlocation){
        LocationDTO location = new LocationDTO(x, y, strlocation);
        mlocationReference.setValue(location);

        //동네예보 70줄 url
        weather_url = weatherendpoint + "serviceKey=" + key + "&base_date=" + weather_date + "&base_time=0200"
                + "&nx=" + x + "&ny=" + y + "&numOfRows=70&_type=xml";
        //단기예보 30줄 url
        forecast_url = forecastendpoint + "serviceKey=" + key + "&base_date=" + weather_date + "&base_time=" + weather_time
                + "&nx=" + x + "&ny=" + y + "&numOfRows=30&_type=xml";
        //단기실황 8줄 url
        current_url = currentendpoint + "serviceKey=" + key + "&base_date=" + weather_date + "&base_time=" + weather_time
                + "&nx=" + x + "&ny=" + y + "&numOfRows=8&_type=xml";
    }


    // 전화번호 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }


    //동네예보정보조회 가져오기
    private class GetXMLLocalWeather extends AsyncTask<String, Void, Document>{
        @Override
        protected Document doInBackground(String... strings) {
            URL url;
            Document doc = null;
            try {
                url = new URL(weather_url);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();
            } catch (Exception e){
                Toast.makeText(getActivity(), "local weather Parsing Error", LENGTH_SHORT).show();
            }
            return doc;
        }


        @Override
        protected void onPostExecute(Document document) {
            String stmn = "";
            String stmx = "";
            NodeList nodeList = document.getElementsByTagName("item");
            int r_p = 0;
            int r = 0;

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element fstElmnt = (Element) node;

                NodeList idx = fstElmnt.getElementsByTagName("category");
                // 모든 category 값들을 출력 위한
                // s += "category = "+  idx.item(0).getChildNodes().item(0).getNodeValue() +"\n";

                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("TMN")) {
                    NodeList gugun = fstElmnt.getElementsByTagName("fcstValue");
                    stmn += "최저: " + gugun.item(0).getChildNodes().item(0).getNodeValue() + "℃";
                }

                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("TMX")) {
                    NodeList gugun = fstElmnt.getElementsByTagName("fcstValue");
                    stmx += "최고: " + gugun.item(0).getChildNodes().item(0).getNodeValue() + "℃";
                }

                // 강수확률 PDP, fcstValue 강수확률에 해당하는 값
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("POP")) {
                    NodeList gugun = fstElmnt.getElementsByTagName("fcstValue");
                    //s += "강수확률 = " + gugun.item(0).getChildNodes().item(0).getNodeValue() + "% \n";
                    if (r_p < 7){
                        rain_probabillity[r] = gugun.item(0).getChildNodes().item(0).getNodeValue() + "%";
                        r_p++;
                    }
                }

                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("PTY")) {
                    NodeList gugun = fstElmnt.getElementsByTagName("fcstValue");
                    int rain_num = Integer.parseInt(gugun.item(0).getChildNodes().item(0).getNodeValue());

                    if(r < 7){
                        if (rain_num == 0) { rain[r] = "없음";}
                        else if (rain_num == 1) { rain[r] = "비";}
                        else if (rain_num == 2) { rain[r] = "비/눈";}
                        else if (rain_num == 3) { rain[r] = "눈";}
                        r++;
                    }
                }

                tmn.setText(stmn);
                tmx.setText(stmx);
                super.onPostExecute(document);
            }
        }
    }

    //초단기예보조회 가져오기
    private class GetXMLforecastWeather extends AsyncTask<String, Void, Document>{
        @Override
        protected Document doInBackground(String... strings) {
            URL url;
            Document doc = null;
            try {
                url = new URL(forecast_url);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();
            } catch (Exception e){
                Toast.makeText(getActivity(), "forecast Parsing Error", LENGTH_SHORT).show();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document document) {
            String s = "";
            NodeList nodeList = document.getElementsByTagName("item");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element fstElmnt = (Element) node;
                String h = strhour + "00";

                NodeList idx = fstElmnt.getElementsByTagName("category");
                NodeList time =  fstElmnt.getElementsByTagName("fcstTime");
                // 모든 category 값들을 출력 위한
                // s += "category = "+ idx.item(0).getChildNodes().item(0).getNodeValue() +"\n";

                // 낙뢰 LGT, fcstValue 낙뢰에 해당하는 값
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("LGT") && time.item(0).getChildNodes().item(0).getNodeValue().equals(h)) {
                    NodeList gugun = fstElmnt.getElementsByTagName("fcstValue");
                    s += "낙뢰 = " + gugun.item(0).getChildNodes().item(0).getNodeValue() + "% \n";
                }

                // 구름상태(하늘상태) SKY, fcstValue 구름상태에 해당하는 값
                // 0~2 : 맑음, 3~5 : 구름조금, 6~8 : 구름많음, 9~10 : 흐림
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("SKY") && time.item(0).getChildNodes().item(0).getNodeValue().equals(h)) {
                    NodeList gugun = fstElmnt.getElementsByTagName("fcstValue");
                    int cloud_num = Integer.parseInt(gugun.item(0).getChildNodes().item(0).getNodeValue());

                    if (cloud_num == 0 || cloud_num == 1 || cloud_num == 2) {
                        sky = 0;
                        s += "하늘상태 = 맑음\n";
                    } else if (cloud_num == 3 || cloud_num == 4 || cloud_num == 5) {
                        sky = 1;
                        s += "하늘상태 = 구름 조금\n";
                    } else if (cloud_num == 6 || cloud_num == 7 || cloud_num == 8) {
                        sky = 2;
                        s += "하늘상태 = 구름 많음\n";
                    } else if (cloud_num == 9 || cloud_num == 10) {
                        sky = 3;
                        s += "하늘상태 = 흐림\n";
                        // s += "fcstValue 하늘상태 = "+  gugun.item(0).getChildNodes().item(0).getNodeValue() +"\n";
                    }
                }

                super.onPostExecute(document);
            }
        }
    }

    //초단기실황조회 가져오기
    private class GetXMLCurrentWeather extends AsyncTask<String, Void, Document>{
        @Override
        protected Document doInBackground(String... strings) {
            URL url;
            Document doc = null;
            try {
                url = new URL(current_url);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();
            } catch (Exception e){
                Toast.makeText(getActivity(), "forecast Parsing Error", LENGTH_SHORT).show();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document document) {
            String s = "";
            NodeList nodeListCurrent = document.getElementsByTagName("item");

            for (int i = 0; i < nodeListCurrent.getLength(); i++) {
                Node node = nodeListCurrent.item(i);
                Element fstElmnt = (Element) node;

                NodeList idx = fstElmnt.getElementsByTagName("category");
                // 모든 category 값들을 출력 위한
                // s += "category = "+  idx.item(0).getChildNodes().item(0).getNodeValue() +"\n";

                // 온도(1시간 기온) T1H, fcstValue 온도에 해당하는 값
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("T1H")) {
                    NodeList gugun = fstElmnt.getElementsByTagName("obsrValue");
                    s += "현재 기온: " + gugun.item(0).getChildNodes().item(0).getNodeValue() + "℃";
                }

                // 강수량 RN1, fcstValue 강수확률에 해당하는 값
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("RN1")) {
                    NodeList gugun = fstElmnt.getElementsByTagName("obsrValue");
                    //s += "강수량 = " + gugun.item(0).getChildNodes().item(0).getNodeValue() + "mm \n";
                }

                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("PTY")) {
                    NodeList gugun = fstElmnt.getElementsByTagName("obsrValue");
                    precipitation = Integer.parseInt(gugun.item(0).getChildNodes().item(0).getNodeValue());
                }

                current.setText(s);
                super.onPostExecute(document);
            }
        }
    }

    //체감온도 가져오기
    private class GetXMLApparentTem extends AsyncTask<String, Void, Document>{
        @Override
        protected Document doInBackground(String... strings) {
            URL url;
            Document doc = null;
            try {
                url = new URL(temperature_url);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();
            } catch (Exception e){
                Toast.makeText(getActivity(), "temperature Parsing Error", LENGTH_SHORT).show();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document document) {
            String s = "";
            NodeList nodeListCurrent = document.getElementsByTagName("IndexModel");

            for (int i = 0; i < nodeListCurrent.getLength(); i++) {
                Node node = nodeListCurrent.item(i);
                Element fstElmnt = (Element) node;

                NodeList idx = fstElmnt.getElementsByTagName("h3");
                s = "체감온도: " + idx.item(0).getChildNodes().item(0).getNodeValue() + "℃";

                int tem = Integer.parseInt(idx.item(0).getChildNodes().item(0).getNodeValue());
                String cloth = "오늘의 옷차림: ";
                if (tem > 28){
                    cloth += "민소매, 반팔, 반바지, 원피스";
                } else if (tem > 23){
                    cloth += "반팔, 얇은 셔츠, 반바지, 면바지";
                } else if (tem > 20){
                    cloth += "얇은 가디건, 긴팔, 면바지, 청바지";
                } else if (tem > 17){
                    cloth += "얇은 니트, 맨투맨, 가디건, 청바지";
                } else if (tem > 12){
                    cloth += "자켓, 가디건, 야상, 청바지, 면바지";
                } else if (tem > 9){
                    cloth += "자켓, 트렌치코트, 야상, 니트, 청바지";
                } else if (tem > 5){
                    cloth += "코트, 가죽자켓, 히트텍, 니트, 레깅스";
                } else {
                    cloth += "패딩, 두꺼운코트, 목도리, 기모제품";
                }

                temperature.setText(s);
                temcloth.setText(cloth);
                super.onPostExecute(document);
            }
        }
    }


    //날씨에 따른 이미지
    public void weatherIcon(){
        switch (precipitation){
            case 0: //강수형태 : 없음
                if(sky == 0){ //하늘상태 : 맑음
                    weather_Icon.setImageResource(R.drawable.w_sunny);
                } else { //하늘상태 : 구름/흐림
                    if(!w_wind){ //바람 false
                        if (!w_lightning) { //번개 false
                            if (sky == 1) { weather_Icon.setImageResource(R.drawable.w_sunny_overcast); //하늘상태 : 구름 적음
                            } else if (sky == 2) { weather_Icon.setImageResource(R.drawable.w_cloud); //하늘상태 : 구름많음
                            } else { weather_Icon.setImageResource(R.drawable.w_cloudy); }//하늘상태 : 흐림
                        }else { weather_Icon.setImageResource(R.drawable.w_lightning); } //번개 true
                    } else {//바람 true
                        if(sky == 3) { weather_Icon.setImageResource(R.drawable.w_cloudy_gusts); //하늘상태 : 흐림
                        } else { weather_Icon.setImageResource(R.drawable.w_cloudy_windy); } ////하늘상태 : 구름
                    }
                }
                break;
            case 1: //강수형태 : 비
                if(w_lightning){ //번개 true
                    weather_Icon.setImageResource(R.drawable.w_thunderstorm);
                } else{ //번개 false
                    if(w_wind){ weather_Icon.setImageResource(R.drawable.w_rain_wind); //바람 true
                    } else { weather_Icon.setImageResource(R.drawable.w_rain); } //바람 false
                }
                break;
            case 2: //강수형태 : 비/눈
                if(!w_lightning) // 번개 false
                    weather_Icon.setImageResource(R.drawable.w_rain_mix);
                else //번개 true
                    weather_Icon.setImageResource(R.drawable.w_sleet_storm);
                break;
            case 3: //강수형태 : 눈
                if(!w_lightning) // 번개 false
                    weather_Icon.setImageResource(R.drawable.w_snow);
                else //번개 true
                    weather_Icon.setImageResource(R.drawable.w_sleet_storm);
                break;
            default:
                weather_Icon.setImageResource(R.drawable.w_sunny);
                break;
        }
    }

    //위경도 <-> 좌표 변환
    private LatXLngY convertGRID_GPS(int mode, double lat_X, double lng_Y)
    {
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기1준점 Y좌표(GRID)

        //
        // LCC DFS 좌표변환 (위경도->좌표, lat_X:위도,  lng_Y:경도)
        //

        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        LatXLngY rs = new LatXLngY();


        if (mode == TO_GRID) {
            rs.lat = lat_X;
            rs.lng = lng_Y;
            double ra = Math.tan(Math.PI * 0.25 + (lat_X) * DEGRAD * 0.5);
            ra = re * sf / Math.pow(ra, sn);
            double theta = lng_Y * DEGRAD - olon;
            if (theta > Math.PI) theta -= 2.0 * Math.PI;
            if (theta < -Math.PI) theta += 2.0 * Math.PI;
            theta *= sn;
            rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
            rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
        }
        else {
            rs.x = lat_X;
            rs.y = lng_Y;
            double xn = lat_X - XO;
            double yn = ro - lng_Y + YO;
            double ra = Math.sqrt(xn * xn + yn * yn);
            if (sn < 0.0) {
                ra = -ra;
            }
            double alat = Math.pow((re * sf / ra), (1.0 / sn));
            alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

            double theta = 0.0;
            if (Math.abs(xn) <= 0.0) {
                theta = 0.0;
            }
            else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI * 0.5;
                    if (xn < 0.0) {
                        theta = -theta;
                    }
                }
                else theta = Math.atan2(xn, yn);
            }
            double alon = theta / sn + olon;
            rs.lat = alat * RADDEG;
            rs.lng = alon * RADDEG;
        }
        return rs;
    }

    class LatXLngY
    {
        public double lat;
        public double lng;

        public double x;
        public double y;

    }


    //위도와 경도로 위치주소 구하기
    public void getLocation(double lat, double lng){
        String str = null;
        Geocoder geocoder = new Geocoder(getActivity(), Locale.KOREA);

        List<Address> address;
        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1);
                if (address != null && address.size() > 0) {
                    str = address.get(0).getAddressLine(0).toString();

                    str = str.replaceAll(" [0-9]", "");
                    str = str.replaceAll("[0-9]", "");
                    str = str.replaceAll("-", "");
                    str = str.replaceAll("대한민국 ", "");
                }
            }
        } catch (IOException e) {
            //Log.e("MainActivity", "주소를 찾지 못하였습니다.");
            Toast.makeText(getActivity(), "location Error",
                    LENGTH_SHORT).show();
            e.printStackTrace();
        }


        location.setText(str);
    }

}