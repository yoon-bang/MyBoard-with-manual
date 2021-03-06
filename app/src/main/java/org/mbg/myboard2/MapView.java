package org.mbg.myboard2;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MapView extends Fragment implements OnMapReadyCallback{
    ViewGroup viewGroup;
    //??????
    double latitude, longitude;
    FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE=1000;
    //db
    FirebaseFirestore db;
    String UserEmail;
    //navigation view
    static DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    static private ArrayList<BoardCafe> mDataset;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView list;
    private RecyclerView.Adapter recyclerAdapter;
    //??????
    Toolbar toolbar;
    SearchView searchView;

    /*static*/
    //NaverMap ??????
    static NaverMap map;
    //FragmentMap?????? ???????????? ?????????- ??????, ??????&??????
    static ArrayList<BoardCafe> cafe_map=new ArrayList<BoardCafe>();
    static ArrayList<Marker> markers;


    static public ArrayList<BoardCafe> get_mDataset(){
        return  mDataset;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        /*db*/
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserEmail=user.getEmail();

        viewGroup = (ViewGroup) inflater.inflate(R.layout.map,container,false);
        /*drawerLayout: main_content & navigation view*/
        drawerLayout = (DrawerLayout) viewGroup.findViewById(R.id.dl_main_drawer_root);
        /*main_content*/
        toolbar=(Toolbar) viewGroup.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);
        //search_view
        searchView=(SearchView)viewGroup.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Search search= new Search(query, getActivity(), getChildFragmentManager());
                try {
                    search.searchBoardCafeData();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }

        });

        /*navigation view*/
        navigationView = (NavigationView) viewGroup.findViewById(R.id.nv_main_navigation_root) ;
        drawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        //??????????????????
        recyclerView  = (RecyclerView)viewGroup.findViewById(R.id.recycler_map);
        layoutManager = new LinearLayoutManager(getActivity()); //?????? ?????? this ???
        recyclerView.setLayoutManager(layoutManager);
        //mDataset: ???????????? ?????? ?????????, ArrayList<BoardCafe>
        mDataset=new ArrayList<>();
        CollectionReference mPostReference =
                (CollectionReference) db.collection("member").document(UserEmail)
                        .collection("LikeCafe");
        mPostReference
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable  QuerySnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            //Toast.makeText(getContext(),"Error loading document",Toast.LENGTH_LONG).show();
                            return;
                        }
                        mDataset.clear();
                        for (QueryDocumentSnapshot doc : snapshot) {    //?
                            BoardCafe likeData=(doc.toObject(BoardCafe.class));
                            mDataset.add(likeData);
                            //Toast.makeText(getContext(),"?????????",Toast.LENGTH_SHORT).show();;
                        }
                        //????????? ??????
                        mAdapter.notifyDataSetChanged();
                    }
                });
        mAdapter = new cafeFavoriteAdapter(getActivity(),mDataset, getChildFragmentManager());
        recyclerView.setAdapter(mAdapter);

        /*????????? ??????*/
        cafe_map=getArguments().getParcelableArrayList("list");
        latitude= getArguments().getDouble("latitude", 0.0);
        longitude=getArguments().getDouble("longitude",0.0);
        /*???????????????*/
        //????????? ?????? ??????
        locationSource=new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        FragmentManager fm = getChildFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        return viewGroup;
    }

    /*??????*/
    public ArrayList<Marker> cafe_to_marker(ArrayList<BoardCafe> cafes){
        ArrayList<Marker> M=new ArrayList<>();

        /*cafes??? ?????? ??????*/
        for (int m = 0; m < cafes.size(); m++) {
            //?????? ??????
            Marker marker = new Marker();
            //?????? ?????? ??????: board_cafe y,x
            marker.setPosition(new LatLng(cafes.get(m).y, cafes.get(m).x));
            //?????? ?????? ??????: board_cafe place_name
            marker.setCaptionText(cafes.get(m).place_name);
            marker.setCaptionColor(Color.rgb(0, 100, 0));
            //?????? ?????? ??????
            marker.setWidth(70);
            marker.setHeight(100);
            //???????????? ?????? add
            M.add(marker);
            //?????? ??????
            M.get(m).setMap(map);
        }
        /*??????????????? ????????? ????????? ??????????????? ??????: markers&cafe_map*/
        for(int m=0;m<M.size();m++) {
            int finalI = m;
            db.collection("cafe").document(cafes.get(m).id)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                //Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            if (snapshot != null && snapshot.exists()) {
                                ArrayList<String> gamelist= (ArrayList<String>) snapshot.get("cafeGameList");
                                if(gamelist!=null ){
                                    if(gamelist.size()>0){
                                        M.get(finalI).setIcon(MarkerIcons.RED);
                                    }
                                }


                            } else {
                                //Log.d(TAG, "Current data: null");
                                //data ??? null ??????
                            }
                        }
                    });
        }

        return M;
    }
    /*????????????- ??? ?????? ?????? ??? ?????? ???????????????*/
    /*??? ?????? ?????? ??????*/
    public Marker cafe_to_marker(BoardCafe cafe){
        //?????? ??????
        Marker marker = new Marker();
        //?????? ?????? ??????: board_cafe y,x
        marker.setPosition(new LatLng(cafe.y, cafe.x));
        //?????? ?????? ??????: board_cafe place_name
        marker.setCaptionText(cafe.place_name);
        marker.setCaptionColor(Color.rgb(0, 100, 0));
        //?????? ?????? ??????
        marker.setWidth(70);
        marker.setHeight(100);

        //?????? ??????
        marker.setMap(map);
        /*??????????????? ????????? ????????? ??????????????? ??????: markers&cafe_map*/
        db.collection("cafe").document(cafe.id)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            //Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            ArrayList<String> gamelist= (ArrayList<String>) snapshot.get("cafeGameList");
                            if(gamelist!=null ){
                                if(gamelist.size()>0){
                                    marker.setIcon(MarkerIcons.RED);
                                }
                            }


                        } else {
                            //Log.d(TAG, "Current data: null");
                            //data ??? null ??????
                        }
                    }
                });

        return marker;
    }

    public InfoWindowDialog set_InfoWindowDialog(BoardCafe cafe){
        InfoWindowDialog I= new InfoWindowDialog(cafe);
        db.collection("cafe").document(cafe.id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                cafeDB cafedata = document.toObject(cafeDB.class);
                                /*?????? ??????*/
                                I.ratingBar01.setRating(cafedata.getStarNumGame());
                                I.textStar01.setText(
                                        ""+(int)(cafedata.getStarNumGame()*10)/(float)10);
                                I.ratingBar02.setRating(cafedata.getStarClean());
                                I.textStar02.setText(
                                        ""+(int)(cafedata.getStarClean()*10)/(float)10);
                                I.ratingBar03.setRating(cafedata.getStarService());
                                I.textStar03.setText(
                                        ""+(int)(cafedata.getStarService()*10)/(float)10);
                                /*????????????&????????????&???????????? ??????*/
                                I.businessHour.setText(
                                        cafedata.getBusinessHour());
                                I.price.setText(
                                        cafedata.getPrice());

                                if(cafedata.getCafeGameList().size()!=0) {
                                    //infoWindowDialogs.get(finalI).cafeGame.setText("???????????? ??????");
                                    ArrayList<String> array_str_cafeGameList= cafedata.getCafeGameList();
                                    String str_cafeGameList="";
                                    for(int i=0;i<array_str_cafeGameList.size();i++){
                                        str_cafeGameList+=array_str_cafeGameList.get(i)+"\n";
                                    }
                                    I.cafeGameList.setText(
                                            str_cafeGameList
                                    );
                                }

                            } else {

                            }
                        } else {
                        }
                    }
                });

        return I;
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        //NaverMap ??????
        map=naverMap;
        /*???????????????*/
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(latitude, longitude));
        naverMap.moveCamera(cameraUpdate);
        /*cafe_map -> markers*/
        markers=cafe_to_marker(cafe_map);

        /*?????? ?????? -> db ???????????? -> ??????????????? ?????????: markers& cafe_map*/
        for (int i = 0; i < markers.size(); i++) {
            //int finalI = i;
            int I=i;
            markers.get(i).setOnClickListener(overlay -> {
                InfoWindowDialog infoWindowDialog_cafe_map= set_InfoWindowDialog(cafe_map.get(I));
                infoWindowDialog_cafe_map.show(getFragmentManager(), InfoWindowDialog.TAG_EVENT_DIALOG);
                return false;
            });
        }

    }


}