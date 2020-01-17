package com.droid.solver.a2020.explore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.droid.solver.a2020.DetailActivity;
import com.droid.solver.a2020.PhysicalArtifactsModel;
import com.droid.solver.a2020.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class ExploreActivity extends AppCompatActivity {

    private SliderView sliderView;
    private Toolbar toolbar;
    private String state,cityName="City name";
    private ImageView cityImage,practicesImages,skillImages;
    private TextView cityTitle,cityDescription,practicesDescription,skillDescription;
    ProgressDialog progressDialog;
    private CardView root;
    private CardView cityCard,practicesCard,skillCard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        state=intent.getStringExtra("state");
        cityName=intent.getStringExtra("city");
        setContentView(R.layout.activity_explore);
        init();

    }

    public void init(){
        sliderView=findViewById(R.id.imageSlider);
        toolbar=findViewById(R.id.toolbar);
//        toolbar.setTitle(cityName.substring(0,1).toUpperCase()+cityName.substring(1));
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        cityImage=findViewById(R.id.city_image);
        cityTitle=findViewById(R.id.city_title);
        cityDescription=findViewById(R.id.city_description);
        practicesImages=findViewById(R.id.practices_images);
        practicesDescription=findViewById(R.id.practices_description);
        skillImages=findViewById(R.id.skill_image);
        skillDescription=findViewById(R.id.skill_description);
        progressDialog=new ProgressDialog(this,R.style.progress_dialog);
        root=findViewById(R.id.root);
        root.setVisibility(View.GONE);
        cityCard=findViewById(R.id.card_view);
        practicesCard=findViewById(R.id.practices_card);
        skillCard=findViewById(R.id.skills_card);
        final CollapsingToolbarLayout collapsingToolbarLayout =  findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(cityName);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        collapsingToolbarLayout.setExpandedTitleTextColor(getResources().getColorStateList(R.color.white,null));
                    }
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
        fetchDetails();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void fetchDetails(){
        if(state!=null && cityName!=null){
            DatabaseReference cityRef= FirebaseDatabase.getInstance().getReference().child("state")
                    .child(state).child(cityName);
            showProgressDialog();
            cityRef.addListenerForSingleValueEvent(listener);

        }else{
            Toast.makeText(this,"State or city is not selected",Toast.LENGTH_SHORT ).show();
            root.setVisibility(View.VISIBLE);
            progressDialog.dismiss();
        }
    }

    private ValueEventListener listener=new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<PhysicalArtifactsModel> artifactsList=new ArrayList<>();

                for(DataSnapshot model : snapshot.child("physicalartifacts").getChildren()){
                   String key=model.getKey();
                   Log.i("TAG", "key : "+key);
                   if(key!=null) {

                       String descripton = model.child("description").getValue(String.class);
                       String image = model.child("image").getValue(String.class);

                       String latitude="0'0";
//                       if(model.child("latitude").getValue()!=null)
//                           latitude = model.child(key).child("latitude").getValue(Double.class);

                       String longitude= "0.0";

//                       if(model.child("longitude").getValue()!=null)
//                          longitude= model.child(key).child("longitude").getValue(Double.class);

                       String name=model.child("name").getValue(String.class);
                       PhysicalArtifactsModel mm=new PhysicalArtifactsModel(image, descripton,latitude,longitude, name);
                       artifactsList.add(mm);
                   }
                }

                 final String cityimage=snapshot.child("cityimage").getValue(String.class);
                 final String cityid=snapshot.child("cityid").getValue(String.class);
                 final String citydescription=snapshot.child("description").getValue(String.class);
                 final String practicesimage=snapshot.child("practices").child("image").getValue(String.class);
                 final String practicesdescription=snapshot.child("practices").child("description").getValue(String.class);
                 final String skillimage=snapshot.child("skills").child("image").getValue(String.class);
                 final String skilldescription=snapshot.child("skills").child("description").getValue(String.class);
                 sliderView.setSliderAdapter(new SliderAdapter(ExploreActivity.this,artifactsList));

                 Picasso.get().load(cityimage).placeholder(R.drawable.trending_item_gradient).into(cityImage);
                 Picasso.get().load(practicesimage).placeholder(R.drawable.trending_item_gradient).into(practicesImages);
                 Picasso.get().load(skillimage).placeholder(R.drawable.trending_item_gradient).into(skillImages);


                 String ss=cityName.substring(0,1).toUpperCase()+cityName.substring(1);
                 cityTitle.setText(ss);
                 cityDescription.setText(citydescription);
                 practicesDescription.setText(practicesdescription);
                 skillDescription.setText(skilldescription);
                 root.setVisibility(View.VISIBLE);
                 progressDialog.dismiss();

                 String uid= FirebaseAuth.getInstance().getUid();
                 if(uid!=null){
                     updateDatabase(cityid,uid,state,cityName,cityimage,citydescription);
                 }
                 cityDescription.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         showDetailActivity(cityimage, cityName, citydescription);
                     }
                 });
                 skillCard.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         showDetailActivity(skillimage, "Skills/Knowledge", skilldescription);
                     }
                 });
                 practicesCard.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         showDetailActivity(practicesimage, "Practices/Culture", practicesdescription);
                     }
                 });

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            progressDialog.dismiss();
            root.setVisibility(View.VISIBLE);
            Toast.makeText(ExploreActivity.this, "Error occured in fetching data from database", Toast.LENGTH_SHORT).show();
        }
    };

    private void showProgressDialog(){
        progressDialog.setTitle("Please wait ");
        progressDialog.setMessage("Loading ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();


    }

    private void showDetailActivity(String url,String title,String description){
        Intent intent=new Intent(this, DetailActivity.class);
        intent.putExtra("image", url);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        startActivity(intent);
    }

    private void updateDatabase(String cityId,String uid,String stateName,String cityName,String cityImage,String cityDescription){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("recommended");
        reference.child(cityId).child("user").child(uid).setValue("visited");
        reference.child(cityId).child("statename").setValue(stateName);
        reference.child(cityId).child("cityname").setValue(cityName);
        reference.child(cityId).child("cityimage").setValue(cityImage);
        reference.child(cityId).child("description").setValue(cityDescription);

    }

}
