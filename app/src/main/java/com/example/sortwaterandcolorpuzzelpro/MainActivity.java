package com.example.sortwaterandcolorpuzzelpro;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private LinearLayout bottleContainer;
    private TextView tvLevel, tvStatus;
    private FrameLayout overlayContainer;
    
    private int currentLevel = 1;
    private List<Bottle> bottles = new ArrayList<>();
    private Integer selectedBottleIndex = null;
    private Stack<List<List<Integer>>> history = new Stack<>();

    private final int MAX_CAPACITY = 4;
    
    // Settings States
    private boolean isSoundEnabled = true;
    private boolean isVibrationEnabled = true;
    private ToneGenerator toneGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        
        initViews();
        loadLevel(currentLevel);
        playPremiumSound(ToneGenerator.TONE_DTMF_0);
    }

    private void initViews() {
        bottleContainer = findViewById(R.id.bottleContainer);
        tvLevel = findViewById(R.id.tvLevel);
        tvStatus = findViewById(R.id.tvStatus);
        overlayContainer = findViewById(R.id.overlayContainer);

        findViewById(R.id.btnRestart).setOnClickListener(v -> restartLevel());
        findViewById(R.id.btnUndo).setOnClickListener(v -> undoMove());
        findViewById(R.id.btnSettings).setOnClickListener(v -> showSettings());
        findViewById(R.id.btnAddBottle).setOnClickListener(v -> addExtraBottle());
        findViewById(R.id.btnTheme).setOnClickListener(v -> Toast.makeText(this, "Themes coming soon!", Toast.LENGTH_SHORT).show());
    }

    private void loadLevel(int level) {
        currentLevel = level;
        tvLevel.setText(getString(R.string.level_format, level));
        tvStatus.setText(R.string.tap_to_pick);
        bottles.clear();
        bottleContainer.removeAllViews();
        history.clear();
        selectedBottleIndex = null;
        findViewById(R.id.btnAddBottle).setEnabled(true);
        findViewById(R.id.btnAddBottle).setAlpha(1.0f);

        int[] availableColors = {
            R.color.liquid_red, R.color.liquid_blue, R.color.liquid_yellow,
            R.color.liquid_green, R.color.liquid_purple, R.color.liquid_orange
        };

        int numColors;
        int numEmpty;

        if (level == 1) {
            numColors = 2;
            numEmpty = 1;
        } else if (level == 2) {
            numColors = 3;
            numEmpty = 2;
        } else if (level == 3) {
            numColors = 4;
            numEmpty = 2;
        } else {
            numColors = 4 + (level / 2);
            if (numColors > availableColors.length) numColors = availableColors.length;
            numEmpty = 2 + (level / 5);
        }
        
        int numBottles = numColors + numEmpty;
        
        List<Integer> allColors = new ArrayList<>();
        for (int i = 0; i < numColors; i++) {
            for (int j = 0; j < MAX_CAPACITY; j++) {
                allColors.add(availableColors[i % availableColors.length]);
            }
        }
        Collections.shuffle(allColors);

        // Pattern logic for Level 10+
        if (level >= 10) {
            setupAdvancedPattern(numBottles, numColors, allColors);
        } else {
            setupStandardPattern(numBottles, numColors, allColors);
        }
    }

    private void setupStandardPattern(int numBottles, int numColors, List<Integer> allColors) {
        LinearLayout row1 = createRow();
        LinearLayout row2 = createRow();
        bottleContainer.addView(row1);
        bottleContainer.addView(row2);

        for (int i = 0; i < numBottles; i++) {
            LinearLayout currentRow = (i < (numBottles + 1) / 2) ? row1 : row2;
            addBottleToRow(currentRow, i, i < numColors, allColors);
        }
    }

    private void setupAdvancedPattern(int numBottles, int numColors, List<Integer> allColors) {
        // Special 16 style pattern: Vertical rows or staggered
        LinearLayout colContainer = new LinearLayout(this);
        colContainer.setOrientation(LinearLayout.HORIZONTAL);
        colContainer.setGravity(android.view.Gravity.CENTER);
        bottleContainer.addView(colContainer);

        LinearLayout leftCol = new LinearLayout(this);
        leftCol.setOrientation(LinearLayout.VERTICAL);
        leftCol.setGravity(android.view.Gravity.CENTER);
        
        LinearLayout centerArea = new LinearLayout(this);
        centerArea.setOrientation(LinearLayout.VERTICAL);
        centerArea.setGravity(android.view.Gravity.CENTER);

        LinearLayout rightCol = new LinearLayout(this);
        rightCol.setOrientation(LinearLayout.VERTICAL);
        rightCol.setGravity(android.view.Gravity.CENTER);

        colContainer.addView(leftCol);
        colContainer.addView(centerArea);
        colContainer.addView(rightCol);

        int bottlesPerSide = numBottles / 3;
        int centerBottles = numBottles - (2 * bottlesPerSide);

        for (int i = 0; i < numBottles; i++) {
            LinearLayout currentLayout;
            if (i < bottlesPerSide) currentLayout = leftCol;
            else if (i < bottlesPerSide + centerBottles) currentLayout = centerArea;
            else currentLayout = rightCol;
            
            addBottleToRow(currentLayout, i, i < numColors, allColors);
        }
    }

    private void addBottleToRow(LinearLayout row, int index, boolean isFilled, List<Integer> allColors) {
        View bottleView = LayoutInflater.from(this).inflate(R.layout.bottle_item, row, false);
        Bottle bottle = new Bottle(bottleView, index);
        if (isFilled) {
            for (int j = 0; j < MAX_CAPACITY; j++) {
                bottle.colors.add(allColors.remove(0));
            }
        }
        bottles.add(bottle);
        updateBottleUI(bottle);
        bottleView.setOnClickListener(v -> onBottleClick(index));
        row.addView(bottleView);
    }

    private LinearLayout createRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(params);
        return row;
    }

    private void onBottleClick(int index) {
        if (selectedBottleIndex == null) {
            if (!bottles.get(index).colors.isEmpty() && !bottles.get(index).isSorted()) {
                selectedBottleIndex = index;
                selectBottleUI(index, true);
                tvStatus.setText(R.string.tap_to_pour);
                playPremiumSound(ToneGenerator.TONE_DTMF_1);
                premiumVibrate(30);
            }
        } else {
            if (selectedBottleIndex == index) {
                selectBottleUI(index, false);
                selectedBottleIndex = null;
                tvStatus.setText(R.string.tap_to_pick);
            } else {
                if (canPour(selectedBottleIndex, index)) {
                    transferColors(bottles.get(selectedBottleIndex), bottles.get(index));
                    selectBottleUI(selectedBottleIndex, false);
                    selectedBottleIndex = null;
                    tvStatus.setText(R.string.tap_to_pick);
                    playPremiumSound(ToneGenerator.TONE_DTMF_2);
                    premiumVibrate(50);
                    checkWin();
                } else {
                    selectBottleUI(selectedBottleIndex, false);
                    if (!bottles.get(index).colors.isEmpty() && !bottles.get(index).isSorted()) {
                        selectedBottleIndex = index;
                        selectBottleUI(selectedBottleIndex, true);
                        tvStatus.setText(R.string.tap_to_pour);
                        playPremiumSound(ToneGenerator.TONE_DTMF_1);
                    } else {
                        selectedBottleIndex = null;
                        tvStatus.setText(R.string.tap_to_pick);
                    }
                }
            }
        }
    }

    private boolean canPour(int fromIndex, int toIndex) {
        Bottle from = bottles.get(fromIndex);
        Bottle to = bottles.get(toIndex);
        if (to.isSorted()) return false;
        if (from.colors.isEmpty()) return false;
        if (to.colors.size() >= MAX_CAPACITY) return false;
        int colorToPour = from.colors.get(from.colors.size() - 1);
        return to.colors.isEmpty() || to.colors.get(to.colors.size() - 1) == colorToPour;
    }

    private void transferColors(Bottle from, Bottle to) {
        int colorToPour = from.colors.get(from.colors.size() - 1);
        int count = 0;
        for (int i = from.colors.size() - 1; i >= 0; i--) {
            if (from.colors.get(i) == colorToPour) count++;
            else break;
        }
        int space = MAX_CAPACITY - to.colors.size();
        int pourAmount = Math.min(count, space);

        if (pourAmount > 0) {
            saveState();
            for (int i = 0; i < pourAmount; i++) {
                from.colors.remove(from.colors.size() - 1);
                to.colors.add(colorToPour);
            }
            updateBottleUI(from);
            updateBottleUI(to);
            checkAndApplyCap(to);
        }
    }

    private void checkAndApplyCap(Bottle bottle) {
        if (bottle.isSorted()) {
            View cap = bottle.view.findViewById(R.id.bottleCap);
            cap.setVisibility(View.VISIBLE);
            cap.setTranslationY(-100f);
            cap.animate()
                    .translationY(0)
                    .setDuration(500)
                    .setInterpolator(new OvershootInterpolator())
                    .start();

            playPremiumSound(ToneGenerator.TONE_DTMF_S);
            premiumVibrate(100);

            ImageView sparkle = bottle.view.findViewById(R.id.sparkleEffect);
            sparkle.setVisibility(View.VISIBLE);
            sparkle.setAlpha(0f);
            sparkle.setScaleX(0.5f);
            sparkle.setScaleY(0.5f);
            sparkle.animate()
                    .alpha(1f)
                    .scaleX(1.5f)
                    .scaleY(1.5f)
                    .setDuration(600)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            sparkle.animate()
                                    .alpha(0f)
                                    .scaleX(2f)
                                    .scaleY(2f)
                                    .setDuration(400)
                                    .setListener(null)
                                    .start();
                        }
                    })
                    .start();
        }
    }

    private void updateBottleUI(Bottle bottle) {
        LinearLayout container = bottle.view.findViewById(R.id.liquidContainer);
        container.removeAllViews();
        
        View cap = bottle.view.findViewById(R.id.bottleCap);
        cap.setVisibility(bottle.isSorted() ? View.VISIBLE : View.GONE);

        for (int i = 0; i < bottle.colors.size(); i++) {
            View layer = new View(this);
            int heightPx = (int) (22 * getResources().getDisplayMetrics().density); 
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, heightPx);
            layer.setLayoutParams(params);
            
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(ContextCompat.getColor(this, bottle.colors.get(i)));
            
            if (i == 0) {
                gd.setCornerRadii(new float[]{0, 0, 0, 0, 28, 28, 28, 28});
            } else {
                gd.setCornerRadius(0);
            }
            
            layer.setBackground(gd);
            container.addView(layer, 0);
        }
    }

    private void selectBottleUI(int index, boolean selected) {
        View view = bottles.get(index).view;
        if (selected) {
            view.animate().translationY(-60f).scaleX(1.05f).scaleY(1.05f).setDuration(250).start();
        } else {
            view.animate().translationY(0f).scaleX(1.0f).scaleY(1.0f).setDuration(250).start();
        }
    }

    private void checkWin() {
        boolean allSorted = true;
        for (Bottle b : bottles) {
            if (b.colors.isEmpty()) continue;
            if (!b.isSorted()) { allSorted = false; break; }
        }
        if (allSorted) {
            playPremiumSound(ToneGenerator.TONE_DTMF_P);
            bottleContainer.postDelayed(this::showVictory, 1000);
        }
    }

    private void showVictory() {
        overlayContainer.setVisibility(View.VISIBLE);
        overlayContainer.removeAllViews();
        View victoryView = LayoutInflater.from(this).inflate(R.layout.layout_victory, overlayContainer, false);
        overlayContainer.addView(victoryView);
        ((TextView)victoryView.findViewById(R.id.btnNextLevel)).setText(getString(R.string.next_level));
        victoryView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up));
        victoryView.findViewById(R.id.btnNextLevel).setOnClickListener(v -> {
            overlayContainer.setVisibility(View.GONE);
            loadLevel(currentLevel + 1);
            playPremiumSound(ToneGenerator.TONE_DTMF_3);
        });
    }

    private void showSettings() {
        overlayContainer.setVisibility(View.VISIBLE);
        overlayContainer.removeAllViews();
        View settingsView = LayoutInflater.from(this).inflate(R.layout.layout_settings, overlayContainer, false);
        overlayContainer.addView(settingsView);
        settingsView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up));

        ImageButton btnSound = settingsView.findViewById(R.id.btnToggleSound);
        ImageButton btnVibrate = settingsView.findViewById(R.id.btnToggleVibration);
        
        updateSettingUI(btnSound, isSoundEnabled);
        updateSettingUI(btnVibrate, isVibrationEnabled);

        btnSound.setOnClickListener(v -> {
            isSoundEnabled = !isSoundEnabled;
            updateSettingUI(btnSound, isSoundEnabled);
            if (isSoundEnabled) playPremiumSound(ToneGenerator.TONE_DTMF_3);
        });

        btnVibrate.setOnClickListener(v -> {
            isVibrationEnabled = !isVibrationEnabled;
            updateSettingUI(btnVibrate, isVibrationEnabled);
            if (isVibrationEnabled) premiumVibrate(50);
        });

        settingsView.findViewById(R.id.btnContinue).setOnClickListener(v -> {
            overlayContainer.setVisibility(View.GONE);
            playPremiumSound(ToneGenerator.TONE_DTMF_3);
        });
    }

    private void updateSettingUI(ImageButton btn, boolean enabled) {
        btn.setAlpha(enabled ? 1.0f : 0.4f);
        if (enabled) {
            btn.setColorFilter(null);
        } else {
            btn.setColorFilter(android.graphics.Color.GRAY, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void playPremiumSound(int tone) {
        if (isSoundEnabled && toneGenerator != null) {
            toneGenerator.startTone(tone, 100);
        }
    }

    private void premiumVibrate(long duration) {
        if (isVibrationEnabled) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(duration);
                }
            }
        }
    }

    private void addExtraBottle() {
        if (bottles.size() >= 14) return;
        saveState();
        LinearLayout targetLayout;
        if (currentLevel >= 10) {
             // In advanced layout, add to the last available vertical layout or center
             targetLayout = (LinearLayout) ((LinearLayout) bottleContainer.getChildAt(0)).getChildAt(1);
        } else {
             targetLayout = (LinearLayout) bottleContainer.getChildAt(1);
        }
        View bottleView = LayoutInflater.from(this).inflate(R.layout.bottle_item, targetLayout, false);
        Bottle bottle = new Bottle(bottleView, bottles.size());
        bottles.add(bottle);
        updateBottleUI(bottle);
        int finalIdx = bottle.index;
        bottleView.setOnClickListener(v -> onBottleClick(finalIdx));
        targetLayout.addView(bottleView);
        findViewById(R.id.btnAddBottle).setEnabled(false);
        findViewById(R.id.btnAddBottle).setAlpha(0.5f);
        playPremiumSound(ToneGenerator.TONE_DTMF_3);
    }

    private void restartLevel() { 
        loadLevel(currentLevel);
        playPremiumSound(ToneGenerator.TONE_DTMF_4);
    }

    private void saveState() {
        List<List<Integer>> state = new ArrayList<>();
        for (Bottle b : bottles) state.add(new ArrayList<>(b.colors));
        history.push(state);
    }

    private void undoMove() {
        if (history.isEmpty()) return;
        List<List<Integer>> lastState = history.pop();
        if (lastState.size() < bottles.size()) {
            Bottle extra = bottles.remove(bottles.size() - 1);
            ((ViewGroup)extra.view.getParent()).removeView(extra.view);
            findViewById(R.id.btnAddBottle).setEnabled(true);
            findViewById(R.id.btnAddBottle).setAlpha(1.0f);
        }
        for (int i = 0; i < lastState.size(); i++) {
            bottles.get(i).colors = new ArrayList<>(lastState.get(i));
            updateBottleUI(bottles.get(i));
        }
        if (selectedBottleIndex != null) { selectBottleUI(selectedBottleIndex, false); selectedBottleIndex = null; }
        tvStatus.setText(R.string.tap_to_pick);
        playPremiumSound(ToneGenerator.TONE_DTMF_B);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            toneGenerator.release();
        }
    }

    private static class Bottle {
        List<Integer> colors = new ArrayList<>();
        View view; int index;
        Bottle(View view, int index) { this.view = view; this.index = index; }
        
        boolean isSorted() {
            if (colors.size() != 4) return false;
            int first = colors.get(0);
            for (int c : colors) if (c != first) return false;
            return true;
        }
    }
}