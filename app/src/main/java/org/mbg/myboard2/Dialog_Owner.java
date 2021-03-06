package org.mbg.myboard2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Dialog_Owner extends androidx.fragment.app.DialogFragment implements View.OnClickListener {
    public static final String TAG_EVENT_DIALOG="dialog_event";
    //int mI = -1;
    private BoardCafe mCafe;
    EditText gameName;
    EditText businessHour;
    EditText price;
    //EditText extra;
    Button button_gameNum;
    Button button_businessHour;
    Button button_price;
    //Button button_extra;
    //db
    FirebaseFirestore db;
    String UserEmail;
    FirebaseUser user;


    public Dialog_Owner(BoardCafe cafe) {
        mCafe=cafe;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_owner, container);
        gameName= (EditText)view.findViewById(R.id.gameName);
        businessHour= (EditText)view.findViewById(R.id.businessHour);
        price=(EditText)view.findViewById(R.id.price);

        button_gameNum=(Button)view.findViewById(R.id.button_gameName);
        button_businessHour=(Button)view.findViewById(R.id.button_businessHour);
        button_price=(Button)view.findViewById(R.id.button_price);



        db=FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        UserEmail=user.getEmail();


        CollectionReference PostRef = (CollectionReference) db.collection("cafe");
        PostRef
                .document(mCafe.id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                //?????? ???????????????
                                cafeDB cafedata=document.toObject(cafeDB.class);
                                businessHour.setText(cafedata.getBusinessHour());
                                price.setText(cafedata.getPrice());
                                //gameName.setText(cafedata.getCafeGameList());
                                ArrayList<String> temp= cafedata.getCafeGameList();
                                String str_temp="";
                                for(int i=0;i<temp.size();i++){
                                    str_temp+=temp.get(i)+",";
                                }
                                gameName.setText(
                                        str_temp
                                );

                            } else {
                            }
                        } else {
                        }
                    }
                });

        button_gameNum.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> input= new ArrayList<String>();
                String str_gameName=gameName.getText().toString();
                int temp=0;
                for(int i=0;i<str_gameName.length();i++){
                    //,
                    if(str_gameName.charAt(i) == ','){
                        if(str_gameName.substring(temp, i).trim().length() != 0){
                            //temp?????? ,?????????
                            input.add(str_gameName.substring(temp, i).trim());
                            //Toast.makeText(getActivity(), ""+str_gameName.substring(temp, i).trim(), Toast.LENGTH_LONG).show();
                            // ,???????????? ?????? ??????
                            temp=i+1;
                        }
                        //emptyString??? ??????
                        else{
                            //Toast.makeText(getActivity(), "emptyString", Toast.LENGTH_LONG).show();
                            // ,???????????? ?????? ??????
                            temp=i+1;
                        }
                    }
                    //?????????
                    else if(i==str_gameName.length()-1){
                        if(str_gameName.substring(temp, i+1).trim().length() != 0){
                            //temp?????? ???????????????
                            input.add(str_gameName.substring(temp, i+1).trim());
                            //Toast.makeText(getActivity(), ""+str_gameName.substring(temp, i+1).trim(), Toast.LENGTH_LONG).show();
                        }
                        //emptyString??? ??????
                        else{
                            //Toast.makeText(getActivity(), "emptyString", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                if (input.size() != 0) {
                    CollectionReference PostRef = (CollectionReference) db.collection("cafe");
                    PostRef
                            .document(mCafe.id)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            //?????? ???????????????
                                            //Toast.makeText(getActivity(),gameName.getText()+", ?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                            DocumentReference docRef=db.collection("cafe").document(mCafe.id);
                                            /*for(int i=0;i<input.size();i++) {
                                                docRef.update("cafeGameList", FieldValue.arrayUnion(input.get(i)));
                                            }*/
                                            docRef.update("cafeGameList", input);
                                            //dismiss();

                                        } else {
                                            //?????? ?????? ??????
                                            // ???????????? = clean,???????????????,????????? ,, ??? ??????
                                            cafeDB cafe=new cafeDB(mCafe.place_name,0,
                                                    0, 0, 0, input, "", ""
                                                    ,mCafe.address_name,
                                                    mCafe.phone);
                                            db.collection("cafe").document(mCafe.id).set(cafe);
                                            //Toast.makeText(getActivity(), "cafe ????????? ????????????"+MapView.cafe_map.get(i).id, Toast.LENGTH_LONG).show();
                                            //dismiss();
                                        }
                                        Toast.makeText(getActivity(), "?????????????????????.", Toast.LENGTH_SHORT).show();


                                    } else {
                                    }
                                }
                            });
                }else{
                    Toast.makeText(getActivity(), "????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show();
                }
            }
        });



        button_businessHour.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (businessHour.getText().toString().trim().length() != 0) {
                    CollectionReference PostRef = (CollectionReference) db.collection("cafe");
                    PostRef
                            .document(mCafe.id)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            //?????? ???????????????
                                            //Toast.makeText(getActivity(),businessHour.getText()+", ?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                            //?????? ???????????? ?????? = ??? num++1 , ?????????
                                            DocumentReference docRef = db.collection("cafe").document(mCafe.id);
                                            docRef.update("businessHour", businessHour.getText().toString().trim());
                                            //dismiss();

                                        } else {
                                            //?????? ?????? ??????
                                            // ???????????? = clean,???????????????,????????? ,, ??? ??????
                                            cafeDB cafe = new cafeDB(mCafe.place_name, 0,
                                                    0, 0, 0, new ArrayList<>(), businessHour.getText().toString().trim(), ""
                                                    ,mCafe.address_name,
                                                    mCafe.phone);
                                            db.collection("cafe").document(mCafe.id).set(cafe);
                                            //dismiss();
                                        }
                                        Toast.makeText(getActivity(), "?????????????????????.", Toast.LENGTH_SHORT).show();

                                    } else {
                                    }
                                }
                            });
                }else{
                    Toast.makeText(getActivity(), "????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        button_price.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (price.getText().toString().trim().length() != 0) {
                    CollectionReference PostRef = (CollectionReference) db.collection("cafe");
                    PostRef
                            .document(mCafe.id)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            //?????? ???????????????
                                            //Toast.makeText(getActivity(),businessHour.getText()+", ?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                            //?????? ???????????? ?????? = ??? num++1 , ?????????
                                            DocumentReference docRef = db.collection("cafe").document(mCafe.id);
                                            docRef.update("price", price.getText().toString().trim());
                                            //dismiss();

                                        } else {
                                            //?????? ?????? ??????
                                            // ???????????? = clean,???????????????,????????? ,, ??? ??????
                                            cafeDB cafe = new cafeDB(mCafe.place_name, 0,
                                                    0, 0, 0, new ArrayList<>(), "", price.getText().toString().trim()
                                                    ,mCafe.address_name,
                                                    mCafe.phone);
                                            db.collection("cafe").document(mCafe.id).set(cafe);
                                            //dismiss();
                                        }
                                        Toast.makeText(getActivity(), "?????????????????????.", Toast.LENGTH_SHORT).show();

                                    } else {
                                    }
                                }
                            });
                }else{
                    Toast.makeText(getActivity(), "????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }
}