package moe.kotohana.randomfood;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import moe.kotohana.randomfood.databinding.ActivityMainBinding;
import moe.kotohana.randomfood.utils.NetworkHelper;
import moe.kotohana.randomfood.utils.RandomHelper;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setCardViewClick();
        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), UserSettingsActivity.class));
            }
        });
        binding.selectRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), NearFoodActivity.class)
                        .putExtra("foodType", RandomHelper.Companion.getRandomNumber(6)));
            }
        });
    }

    private void setCardViewClick() {
        binding.hansikFood.setOnClickListener(new CardClickListener().setPosition(0));
        binding.chineseFood.setOnClickListener(new CardClickListener().setPosition(1));
        binding.boonsikFood.setOnClickListener(new CardClickListener().setPosition(2));
        binding.chickenFood.setOnClickListener(new CardClickListener().setPosition(3));
        binding.fastFood.setOnClickListener(new CardClickListener().setPosition(4));
        binding.pizzaFood.setOnClickListener(new CardClickListener().setPosition(5));
        binding.japaneseFood.setOnClickListener(new CardClickListener().setPosition(6));
    }

    private class CardClickListener implements View.OnClickListener {
        int position;

        @Override
        public void onClick(View v) {
            startActivity(new Intent(getApplicationContext(), NearFoodActivity.class)
                    .putExtra("foodType", position));
        }

        View.OnClickListener setPosition(int position) {
            this.position = position;
            return this;
        }

    }

}
