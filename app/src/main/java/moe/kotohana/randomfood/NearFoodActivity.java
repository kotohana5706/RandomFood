package moe.kotohana.randomfood;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.nitrico.lastadapter.Holder;
import com.github.nitrico.lastadapter.ItemType;
import com.github.nitrico.lastadapter.LastAdapter;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import moe.kotohana.randomfood.databinding.ActivityNearFoodBinding;
import moe.kotohana.randomfood.databinding.ListItemBinding;
import moe.kotohana.randomfood.models.History;
import moe.kotohana.randomfood.models.Items;
import moe.kotohana.randomfood.models.Location;
import moe.kotohana.randomfood.models.Place;
import moe.kotohana.randomfood.models.Restaurant;
import moe.kotohana.randomfood.utils.GPSService;
import moe.kotohana.randomfood.utils.MathHelper;
import moe.kotohana.randomfood.utils.NetworkHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NearFoodActivity extends AppCompatActivity {

    /*
    * 한식 0
    * 중식 1
    * 분식 2
    * 치킨 3
    * 패스트푸드 4
    * 피자 5
    * 일식 6
    * */
    boolean isLoaded = false;
    LastAdapter adapter;
    int type = 0;
    GPSService service;
    ActivityNearFoodBinding binding;
    MaterialDialog progressDialog;
    private ArrayList<Restaurant> arrayList = new ArrayList<>();
    private String[] typeList = new String[]{
            "한식",
            "중식",
            "분식",
            "치킨",
            "패스트푸드",
            "피자",
            "일식"
    };
    private String[] typeQueryList = new String[]{
            "한식 음식점",
            "중식 음식점",
            "분식 음식점",
            "치킨 음식점",
            "패스트푸드",
            "피자",
            "일식 음식점"
    };


    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_near_food);
        setSupportActionBar(binding.toolbar);
        type = getIntent().getIntExtra("foodType", 0);
        binding.toolbar.setTitleTextColor(Color.WHITE);
        binding.toolbar.setSubtitleTextColor(Color.WHITE);
        binding.nearRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("이 주변의 " + typeList[type]);


        getPlace();
    }

    private void getPlace() {
        progressDialog = new MaterialDialog.Builder(this)
                .title("데이터를 로드하는 중입니다")
                .progress(true, 0)
                .content("잠시만 기다려주세요.")
                .cancelable(false)
                .show();
        service = new GPSService(this);
        android.location.Location location = service.getLocation();
        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            NetworkHelper.Companion.getNetworkInstance().getAddressByGeocode(longitude + "," + latitude).enqueue(new Callback<Location>() {
                @Override
                public void onResponse(Call<Location> call, Response<Location> response) {
                    switch (response.code()) {
                        case 200:
                            Items items = response.body().getResult().getItems().get(0);
                            getSupportActionBar().setSubtitle(
                                    items.getAddrdetail().getSido() + " " +
                                            items.getAddrdetail().getSigugun() + " " +
                                            items.getAddrdetail().getDongmyun()
                            );
                            NetworkHelper.Companion.getNetworkInstance().getRestaurant(items.getAddrdetail().getSigugun() + " " + items.getAddrdetail().getDongmyun() + " " + typeQueryList[type], 20).enqueue(new Callback<Place>() {
                                @Override
                                public void onResponse(Call<Place> call, Response<Place> response) {
                                    switch (response.code()) {
                                        case 200:
                                            arrayList = response.body().getItems();
                                            initializeLayout(isLoaded);
                                            break;
                                        default:
                                            Log.e("getRestaurant", "onResponse : " + response.code() + "");
                                    }
                                }

                                @Override
                                public void onFailure(Call<Place> call, Throwable t) {
                                    progressDialog.dismiss();
                                    Log.e("asdf", "onfailure  : " + t.getLocalizedMessage());
                                    finishWithFailure();
                                }
                            });
                            break;
                        default:
                            Log.e("getAddressByGeocode", "onResponse : " + response.code() + "");
                            finishWithFailure();
                    }
                }

                @Override
                public void onFailure(Call<Location> call, Throwable t) {
                    progressDialog.dismiss();
                    Log.e("asdf", "onfailure  : " + t.getLocalizedMessage());
                    finishWithFailure();

                }
            });
        } else {
            service.showSettingsAlert(this);
        }
    }

    private void initializeLayout(boolean isLoadedOnce) {
        if (!isLoadedOnce) {
            adapter = new LastAdapter(arrayList, moe.kotohana.randomfood.BR.content)
                    .map(Restaurant.class, new ItemType<ListItemBinding>(R.layout.list_item) {
                        @Override
                        public void onBind(Holder<ListItemBinding> holder) {
                            super.onBind(holder);
                            holder.getBinding().setActivity(NearFoodActivity.this);
                            holder.getBinding().setPosition(holder.getLayoutPosition());
                        }
                    })
                    .into(binding.nearRecyclerView);
            isLoaded = true;

        } else {
            adapter.notifyDataSetChanged();
        }
        progressDialog.dismiss();
        (findViewById(R.id.random)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onListClick(MathHelper.Companion.getRandomNumber(arrayList.size()));
            }
        });
    }

    public void finishWithFailure() {
        Toast.makeText(this, "데이터 로드에 실패하였습니다.\n인터넷 연결 상태를 확인 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
        finish();
    }


    public void onListClick(int position) {
        ArrayList<Restaurant> tempArr = new ArrayList<>();
        tempArr.add(arrayList.get(position));
        startActivity(new Intent(getApplicationContext(), NearFoodMapActivity.class)
                .putExtra("restaurants", tempArr)
                .putExtra("toolbar", getSupportActionBar().getTitle()));
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<History> history = realm.where(History.class).findAll();
        if (history.size() == 0) {
            History data = realm.createObject(History.class);
            RealmList<Restaurant> list = new RealmList<>();
            if (list.isManaged())
                list.add(arrayList.get(position).setRealType(type));
            else
                list.add(realm.copyToRealm(arrayList.get(position).setRealType(type)));
            data.setHistoryList(list);
        } else {
            history.get(0).getHistoryList().add(arrayList.get(position).setRealType(type));
        }
        realm.commitTransaction();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.near_food_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.map:
                startActivity(new Intent(getApplicationContext(), NearFoodMapActivity.class)
                        .putExtra("restaurants", arrayList)
                        .putExtra("toolbar", getSupportActionBar().getTitle()));

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLoaded) {
            getPlace();
        }
    }
}
