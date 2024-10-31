package com.sumit1334.recyclerviewpager;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.HVArrangement;
import com.google.appinventor.components.runtime.HorizontalArrangement;
import com.google.appinventor.components.runtime.ReplForm;
import com.google.appinventor.components.runtime.VerticalArrangement;
import com.sumit1334.recyclerviewpager.lib.RecyclerViewPager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class RecyclerPager extends AndroidNonvisibleComponent implements Component {
    private final String TAG = "Recycler Pager";
    private final Activity context;
    private final MyAdapter adapter;
    private final HashMap<String, MyAdapter.SimpleViewHolder> viewHolder = new HashMap<>();
    private final ArrayList<AndroidViewComponent> views = new ArrayList<>();
    private final boolean isCompanion;
    private final RecyclerViewPager recyclerView;
    private int total = 0;
    private String orientation;

    public RecyclerPager(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
        this.isCompanion = form instanceof ReplForm;
        recyclerView = new RecyclerViewPager(context);
        this.adapter = new MyAdapter();
        recyclerView.setLayoutParams(new RecyclerView.LayoutParams(-1, -1));
        recyclerView.setTriggerOffset(0.15f);
        recyclerView.setFlingFactor(0.25f);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLongClickable(true);
        recyclerView.setSinglePageFling(false);
        Orientation("Vertical");
        recyclerView.addOnPageChangedListener(new RecyclerViewPager.OnPageChangedListener() {
            @Override
            public void OnPageChanged(int oldPosition, int newPosition) {
                PageChanged(oldPosition + 1, newPosition + 1);
            }
        });
    }

    @SimpleProperty(description = "Defines the orientation of the viewpager")
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHOICES, defaultValue = "Vertical", editorArgs = {"Horizontal", "Vertical"})
    public void Orientation(String orientation) {
        this.orientation = orientation;
    }

    @SimpleProperty
    public String Orientation() {
        return orientation;
    }

    @SimpleEvent(description = "This event raises when user swiped and page changes. Returns the old page and new page that is active")
    public void PageChanged(int oldPosition, int newPosition) {
        EventDispatcher.dispatchEvent(this, "PageChanged", oldPosition, newPosition);
    }

    @SimpleEvent(description = "You would need to bind your pages in order to show the content of the page. You must read about recycler view first before using recycler views in your apps.")
    public void BindView(Object parent, String id, int position) {
        EventDispatcher.dispatchEvent(this, "BindView", parent, id, position);
    }

    @SimpleEvent(description = "This event raises when the view pager wanted to created component. Just make your design in this event and then add it to the parent by using AddView block.")
    public void CreateComponent(Object parent) {
        EventDispatcher.dispatchEvent(this, "CreateComponent", parent);
    }

    @SimpleFunction(description = "Initialize or creates the viewpager in given container")
    public void CreatePager(HVArrangement in) {
        recyclerView.setAdapter(this.adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, Orientation().equals("Vertical") ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL, false));
        ((LinearLayout) ((ViewGroup) in.getView()).getChildAt(0)).addView(recyclerView);
        Log.i(TAG, "CreatePager: Pager created in " + in);
    }

    @SimpleFunction(description = "After creating the page's design in CreateComponent event you must have to add your create page in the parent of recycler view. You can use this block to add your view to the parent of recycler view.")
    public void AddView(Object parent, AndroidViewComponent component, String id) {
        if (parent instanceof MyAdapter.SimpleViewHolder) {
            this.viewHolder.put(id, ((MyAdapter.SimpleViewHolder) parent));
            LinearLayout layout = ((MyAdapter.SimpleViewHolder) parent).layout;
            ViewGroup viewGroup = (ViewGroup) component.getView().getParent();
            viewGroup.removeView(component.getView());
            layout.addView(component.getView());
            ((MyAdapter.SimpleViewHolder) parent).id = id;
            views.add(component);
        }
    }

    @SimpleFunction(description = "Add a simple click listener on the given component with id and parent. This will help you to listen the clicks on your component with position easily.")
    public void AddClickListener(AndroidViewComponent component, Object parent, String id) {
        if (parent instanceof MyAdapter.SimpleViewHolder) {
            View view = getFinalView(component);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ItemClicked(((MyAdapter.SimpleViewHolder) parent).getAdapterPosition() + 1, component, id, parent);
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ItemLongClicked(((MyAdapter.SimpleViewHolder) parent).getAdapterPosition() + 1, component, id, parent);
                    return false;
                }
            });
        }
    }

    @SimpleEvent(description = "This event raises when any component is clicked. Make sure to add click listener first by using AddClickListener block")
    public void ItemClicked(int position, AndroidViewComponent component, String id, Object parent) {
        EventDispatcher.dispatchEvent(this, "ItemClicked", position, component, id, parent);
    }

    @SimpleEvent(description = "This event raises when any component is long clicked. Make sure to add click listener first by using AddClickListener block")
    public void ItemLongClicked(int position, AndroidViewComponent component, String id, Object parent) {
        EventDispatcher.dispatchEvent(this, "ItemLongClicked", position, component, id, parent);
    }

    private View getFinalView(AndroidViewComponent component) {
        View view = component.getView();
        final String className = component.getClass().getSimpleName();
        if (className.equals("MakeroidCardView")) {
            ViewGroup viewGroup = (ViewGroup) view;
            view = viewGroup.getChildAt(0);
        } else if (className.equals(HorizontalArrangement.class.getSimpleName()) || className.equalsIgnoreCase(VerticalArrangement.class.getSimpleName())) {
            Method[] methods = component.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase("IsCard")) {
                    if (!method.getReturnType().getName().equalsIgnoreCase("void")) {
                        try {
                            final boolean isCard = (boolean) method.invoke(component);
                            if (isCard) {
                                ViewGroup viewGroup = (ViewGroup) view;
                                view = viewGroup.getChildAt(0);
                            }
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            Log.e(TAG, "getFinalView: " + e.getMessage());
                        }
                    }
                }
            }
        }
        return view;
    }

    @SimpleFunction(description = "Add a page at given position. You do not need to give component here. CreateComponent will be called if needed.")
    public void AddItemAt(int position) {
        total++;
        this.adapter.notifyItemInserted(position - 1);
    }

    @SimpleFunction(description = "Removes a page at given")
    public void RemoveItem(int position) {
        this.adapter.notifyItemRemoved(position - 1);
        this.views.remove(position - 1);
    }

    @SimpleFunction(description = "Returns the id from given position. Use this blocks only when the page of this position is active or nearly active")
    public String GetIdFromPosition(int position) {
        return ((MyAdapter.SimpleViewHolder) Objects.requireNonNull(this.recyclerView.findViewHolderForAdapterPosition(position - 1))).id;
    }

    @SimpleFunction(description = "Smooth scrolls the view pager to given position")
    public void Scroll(int position) {
        recyclerView.smoothScrollToPosition(position - 1);
    }

    @SimpleFunction(description = "Returns the current position of the view pager")
    public int GetCurrentPosition() {
        return recyclerView.getCurrentPosition();
    }

    @SimpleFunction(description = "Set the total items of viewpager. You can call this whenever you make changes in your list and after creation")
    public void Total(int total) {
        this.total = total;
        this.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @SimpleProperty
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0.15")
    public void TriggerOffset(float offset) {
        recyclerView.setTriggerOffset(offset);
    }

    @SimpleProperty
    public float TriggerOffset() {
        return recyclerView.getTriggerOffset();
    }

    @SimpleProperty(description = "This property controls the fling animation of viewpager while swiping")
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0.25")
    public void FlingFactor(float factor) {
        recyclerView.setFlingFactor(factor);
    }

    @SimpleProperty
    public float FlingFactor() {
        return recyclerView.getFlingFactor();
    }

    @SimpleProperty(description = "If set to true then user will be able to swipe multiple pages in one try of swipe")
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    public void SinglePageFling(boolean single) {
        recyclerView.setSinglePageFling(single);
    }

    @SimpleProperty
    public boolean SinglePageFling() {
        return recyclerView.isSinglePageFling();
    }

    @SimpleProperty(description = "If true then inertia will be applied to view pager")
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    public void Inertia(boolean single) {
        recyclerView.setInertia(single);
    }

    @SimpleProperty
    public boolean Inertia() {
        return recyclerView.isInertia();
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.SimpleViewHolder> {

        @NonNull
        @NotNull
        @Override
        public SimpleViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int i) {
            SimpleViewHolder simpleViewHolder = new SimpleViewHolder(new LinearLayout(context));
            CreateComponent(simpleViewHolder);
            return simpleViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull SimpleViewHolder simpleViewHolder, int i) {
            BindView(simpleViewHolder, simpleViewHolder.id, i + 1);
        }

        @Override
        public int getItemCount() {
            return total;
        }

        public class SimpleViewHolder extends RecyclerView.ViewHolder {
            final LinearLayout layout;
            String id = "None";

            public SimpleViewHolder(@NonNull @NotNull LinearLayout itemView) {
                super(itemView);
                this.layout = itemView;
                this.layout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            }
        }
    }
}