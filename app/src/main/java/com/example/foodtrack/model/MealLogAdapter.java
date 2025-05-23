package com.example.foodtrack.model;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodtrack.MealLogActivity;
import com.example.foodtrack.R;

import java.util.List;

public class MealLogAdapter extends RecyclerView.Adapter<MealLogAdapter.ViewHolder> {

    private List<MealLogItem> items;
    private Context mContext;

    public MealLogAdapter(Context context, List<MealLogItem> items) {
        this.items = items;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_meal_log_card, parent, false));
    }



    @Override
    public void onBindViewHolder(MealLogAdapter.ViewHolder holder, int position) {
        MealLogItem item = items.get(position);
        holder.bindTo(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView foodName, brand, amountAndCalories, protein, carbs, fat;
        private ImageButton deleteButton, modifyButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            foodName = itemView.findViewById(R.id.foodNameText);
            brand = itemView.findViewById(R.id.brandText);
            amountAndCalories = itemView.findViewById(R.id.amountAndCaloriesText);
            protein = itemView.findViewById(R.id.proteinText);
            carbs = itemView.findViewById(R.id.carbText);
            fat = itemView.findViewById(R.id.fatText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            modifyButton = itemView.findViewById(R.id.modifyButton);

        }

        public void bindTo(MealLogItem currentItem) {
            foodName.setText(currentItem.foodName);
            brand.setText(currentItem.brand);

            String amountAndCaloriesText = itemView.getContext().getString(R.string.mealItemCardAmountAndCalories, currentItem.amount, currentItem.calories);
            amountAndCalories.setText(amountAndCaloriesText);

            String proteinText = itemView.getContext().getString(R.string.protein) + " - " + itemView.getContext().getString(R.string.mealItemCardNutrition, currentItem.protein);
            protein.setText(proteinText);

            String carbsText = itemView.getContext().getString(R.string.carb) + " - " + itemView.getContext().getString(R.string.mealItemCardNutrition, currentItem.carbs);
            carbs.setText(carbsText);

            String fatText = itemView.getContext().getString(R.string.fat) + " - " + itemView.getContext().getString(R.string.mealItemCardNutrition, currentItem.fat);
            fat.setText(fatText);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Activity", "Add cart button clicked!");
                    ((MealLogActivity)mContext).deleteLogItemButtonPressed(currentItem.logId);
                }
            });

            modifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Activity", "Add cart button clicked!");
                    ((MealLogActivity)mContext).modifyLogItemButtonPressed(currentItem.logId);
                }
            });

        }

    }
}

