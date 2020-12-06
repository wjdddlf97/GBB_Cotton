package com.example.cotton.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.cotton.ApiService;
import com.example.cotton.LoginActivity;
import com.example.cotton.MemberInfo;
import com.example.cotton.R;
import com.example.cotton.RetrofitClient;
import com.example.cotton.RetrofitV1;
import com.example.cotton.UserRegisteredBookSaveForm;
import com.example.cotton.bookSaveForm;
import com.example.cotton.firebaseFunction;
import com.example.cotton.ui.food.FoodListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.cotton.ui.home.register.RegisterBookActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

   // Button btnLogout;
    List<MemberInfo> memberInfos = new ArrayList<>();
    List<bookSaveForm> bookSaveFormList= new ArrayList<>();
    List<UserRegisteredBookSaveForm> testList = new ArrayList<>();

    ImageButton home_profile_image_button;//프로필 이미지
    TextView home_my_point_user_name_text_view;//사용자명
    ImageButton home_my_point_plus_btn;//플러스 버튼
    TextView home_my_point_amount_text_view;//현재 보유 포인트 양
    TextView home_my_point_food_ticket_text_view;//보유 식권

    RecyclerView home_my_rented_book_recycler_view;//대여 도서 목록 RecyclerView
    MyRentedBookListAdapter myRentedBookListAdapter;//대여 도서 목록 adapter

    RecyclerView home_my_registered_book_recycler_view;//나의 등록 도서 목록 RecyclerView
    MyRegisteredBookListAdapter myRegisteredBookListAdapter;//나의 등록 도서 목록 adapter

    Button home_register_book_btn;//도서등록 버튼

    double money;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //인플레이션
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        home_profile_image_button=view.findViewById(R.id.home_profile_image_button);
        home_my_point_user_name_text_view=view.findViewById(R.id.home_my_point_user_name_text_view);
        home_my_point_plus_btn=view.findViewById(R.id.home_my_point_plus_btn);
        home_my_point_amount_text_view=view.findViewById(R.id.home_my_point_amount_text_view);
        home_my_point_food_ticket_text_view=view.findViewById(R.id.home_my_point_food_ticket_text_view);

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        home_my_rented_book_recycler_view=view.findViewById(R.id.home_my_rented_book_recycler_view);
        home_my_registered_book_recycler_view=view.findViewById(R.id.home_my_registered_book_recycler_view);

        home_my_rented_book_recycler_view.setNestedScrollingEnabled(false);
        home_my_registered_book_recycler_view.setNestedScrollingEnabled(false);

        //대여 도서 목록 RecyclerView 설정 method
        showMyRentedBookListFunc();

        //나의 도서 목록 RecyclerView 설정 method
        showMyRegisteredBookFunc();

        // firebase_function_ProfileImageDownload + FirebaseFunction 파일 안 설명참조
        // 프로필 사진 받아오기 (주석 지우면 실행됩니다.)
        firebaseFunction firebaseTest = new firebaseFunction();
        firebaseTest.profileImageDownload(home_profile_image_button, this.getContext());


        home_register_book_btn=view.findViewById(R.id.home_register_book_btn);

        home_register_book_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), RegisterBookActivity.class);
                startActivity(intent);
            }
        });

        /*
        // 책 저장 방식입니다.
        // 인자 값으로 (String 바코드, String 책제목, String 이미지링크, String 저자, String 학과, String 등록날짜, int 빌려준 횟수(0으로 초기화해서 사용해주세요.) )
        firebaseFunction firebaseInput = new firebaseFunction();
        firebaseInput.insertBookInfo2("9788959522057", "ARTHAS: RISE OF THE LICH KING", "pictureLink", "크리스티 골든", "흑마법전공", "2020-12-06", 10);
        */



        firebaseTest.myRegisteredBookListGet((resultList) -> { //resultList안에 너가 원하는 모든게 있단다.
                                                               //resultList.get(i).getBookName();
            return null;
        });

        firebaseTest.myRentedBookListGet((resultList) -> {
            return null;
        });




        // Home화면에 UserName 출력
        firebaseTest.profileGet(memberInfos, (resultList) -> {
             Log.d("home에서 확인",resultList.get(0).getName());
            home_my_point_user_name_text_view.setText(resultList.get(0).getName());
             return null;
         });

        // Home화면에 보유티겟 수 출력
        firebaseTest.profileGet(memberInfos, (resultList) -> {
            Log.d("home에서 확인2",Long.toString(resultList.get(0).getTicket()));
            home_my_point_food_ticket_text_view.setText("보유식권: " + Long.toString(resultList.get(0).getTicket()) + "장");
            return null;
        });

        // Home화면에 지갑잔고 출력
        firebaseTest.profileGet(memberInfos, (resultList) -> {

            ApiService call = RetrofitClient.getApiService();

            HashMap<String, String> headerMap = new HashMap<String, String>();
            headerMap.put("Content-Type", "application/json");
            headerMap.put("Authorization", "Pr35dc2sqok4JsPXjRkZ63T1R1MTujVwqfwzNHZBo9Z2oVPDvBbmqdsk28FhLenv"); //Dapp API키값

            call.getMoney(resultList.get(0).getWallet(),headerMap).enqueue(new Callback<RetrofitV1>() {
                @Override
                public void onResponse(Call<RetrofitV1> call, Response<RetrofitV1> response) {
                    Log.d("성공 : ", "result : " + response.body().getResult());
                    Log.d("성공 : ", "address : " + response.body().getDataBalance().getBalance());
                    money = Double.parseDouble(response.body().getDataBalance().getBalance());
                    money = (money*0.000000000000000001);
                    home_my_point_amount_text_view.setText((String.valueOf((int)money)));
                }

                @Override
                public void onFailure(Call<RetrofitV1> call, Throwable t) {
                    Log.d("실패 : ", t.toString());
                }

            });

            return null;
        });

        //로그아웃 버튼 구현
        home_profile_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("로그아웃").setMessage("\n로그아웃 하시겠습니까?\n");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent=new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });

                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Toast.makeText(getContext(), "로그아웃 취소", Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });



//
//         //여기서는 단어하나로 검색가능
//         firebaseTest.serchBook("4");


//         firebaseTest.profileGet(memberInfos, (resultList) -> {  // 맴버 정보 가져오기 / get(0).get 으로 모든정보가져올수있음
//                                                                 // 해당 정보 이용시 여기 안에다 코딩해야함
//             Log.d("home에서 확인",resultList.get(0).getName());
//             return null;
//         });

        /*
        firebaseTest.bookListGet(bookSaveFormList, (resultList) -> { // 모든 책정보 가져오기 / for문을 size로 돌리면 모든 책정보 가져올수 있음
             Log.d("home에서 확인",resultList.get(0).getBookName());
             Log.d("home에서 확인",resultList.get(1).getBookName());

             return null;
         });


        */


//        btnLogout=root.findViewById(R.id.btn_logout);
//        //로그아웃 버튼 구현
//        btnLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FirebaseAuth.getInstance().signOut();
//                Intent intent=new Intent(getActivity(), LoginActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//            }
//        });



        return view;
    }

    //대여 도서 목록 RecyclerView 설정
    public void showMyRentedBookListFunc(){
        //adapter 생성


        firebaseFunction firebaseTest = new firebaseFunction();

        firebaseTest.myRentedBookListGet((resultList) -> {
            myRentedBookListAdapter = new MyRentedBookListAdapter() ;
            home_my_rented_book_recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));
            //adapter 달기
            home_my_rented_book_recycler_view.setAdapter(myRentedBookListAdapter);
            for(int i=0;i<resultList.size();i++){
                myRentedBookListAdapter.addItem(resultList.get(i).getBookName(),resultList.get(i).getBookWriter(),resultList.get(i).getStatus());
            }
            // 리사이클러뷰에 표시할 데이터 리스트 생성(예시로 3개), 추후 firebase에서 data 받아와 동적으로 생성되게 짤 예정
            myRentedBookListAdapter.addItem("C언어 콘서트","김동주","도서대여");
            myRentedBookListAdapter.addItem("Java 콘서트","김민석","연체");
            myRentedBookListAdapter.addItem("C++ 콘서트","이정일","반납대기");
            myRentedBookListAdapter.addItem("C# 콘서트","심민수","반납대기");
            myRentedBookListAdapter.notifyDataSetChanged();//adapter의 변경을 알림
            return null;
        });

    }

    //나의 도서 목록 RecyclerView 설정
    public void showMyRegisteredBookFunc(){
        //adapter 생성


        firebaseFunction firebaseTest = new firebaseFunction();
        firebaseTest.myRegisteredBookListGet((resultList) -> { //resultList안에 너가 원하는 모든게 있단다.
            myRegisteredBookListAdapter = new MyRegisteredBookListAdapter() ;

            home_my_registered_book_recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));
            //adapter 달기
            home_my_registered_book_recycler_view.setAdapter(myRegisteredBookListAdapter);

            for(int i=0;i<resultList.size();i++) {
                myRegisteredBookListAdapter.addItem(resultList.get(i).getBookName(), resultList.get(i).getBookWriter());
            }
            myRegisteredBookListAdapter.addItem("알기 쉽게 해설한 데이터구조","김동주");
            myRegisteredBookListAdapter.addItem("케라스 창시자에게 배우는 딥러닝","김민석");
            myRegisteredBookListAdapter.addItem("JAVA Programming","이정일");

            myRegisteredBookListAdapter.notifyDataSetChanged();//adapter의 변경을 알림
            return null;
        });


        // 리사이클러뷰에 표시할 데이터 리스트 생성(예시로 3개), 추후 firebase에서 data 받아와 동적으로 생성되게 짤 예정

    }

    public void searchMoney(String wallet){




    }
}