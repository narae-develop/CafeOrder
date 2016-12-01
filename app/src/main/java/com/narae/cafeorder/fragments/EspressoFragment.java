package com.narae.cafeorder.fragments;

//import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.narae.cafeorder.R;
import com.narae.cafeorder.menu.MenuListViewAdapter;
import com.narae.cafeorder.menu.MenuListViewItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class EspressoFragment extends Fragment{

    MenuListViewAdapter adapter;
    ListView listview ;

    private View seletedItem;

    String titleStr;
    String descStr;
    String priceStr;

    int tallPrice;

    public EspressoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        String url = getString(R.string.server_url) + "/coffeeinfo";

        // Adapter 생성
        adapter = new MenuListViewAdapter() ;
        listview = (ListView) view.findViewById(R.id.menulistview);

        sendRequest(url);
        settingItemClick();

        return view;
    }

    private void sendRequest(String url) {
        JsonArrayRequest socRequest = new JsonArrayRequest(Request.Method.GET, url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject coffee = response.getJSONObject(i);
                        String eng_name = coffee.getString("engName");
                        String kor_name = coffee.getString("korName");
                        String coffee_type = coffee.getString("coffeeType");
                        String price = coffee.getString("price") + "원";
                        String img_name = coffee.getString("keyName");

                        String uri = "@drawable/" + img_name;
                        String packName = getContext().getPackageName(); // 패키지명
                        int imageResource = getResources().getIdentifier(uri, null, packName);

                        if(coffee_type.equals("ESPRESSO")) {
                            adapter.addItem(ContextCompat.getDrawable(getContext(), imageResource), eng_name, kor_name, price);
                        }

                        // 리스트뷰 참조 및 Adapter달기
                        listview.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("ERROR" + error.getMessage());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(socRequest);

    }

    /**
     * 아이템 선택
     */
    private void settingItemClick() {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                MenuListViewItem item = (MenuListViewItem) parent.getItemAtPosition(position) ;
                titleStr = item.getTitle() ;
                descStr = item.getDesc() ;
                priceStr = item.getPrice();
                priceStr = priceStr.substring(0, priceStr.length()-1);
                //Drawable iconDrawable = item.getIcon();

                if(seletedItem!=null) {
                    nonSelectItemInit(seletedItem);
                }

                if(seletedItem!=v) {
                    seletedItem = v;
                    selectItemInit(v);
                } else {
                    seletedItem = null;
                }
                // TODO : use item data.
            }
        });
    }

    /**
     * 아이템 초기화(미선택영역)
     */
    private void nonSelectItemInit(View v) {
        v.findViewById(R.id.hiddenCount).setVisibility(View.GONE);
        v.findViewById(R.id.hiddenMenuLayout).setVisibility(View.GONE);
        //해당 아이템 초기화
        TextView countText = (TextView)v.findViewById(R.id.countText);
        countText.setText("1");
        v.findViewById(R.id.coffeeICED).setSelected(false);
        TextView priceText = (TextView) v.findViewById(R.id.priceText);
        priceText.setText(String.valueOf(tallPrice) + "원");
    }

    /**
     * 아이템 초기화(선택영역)
     */
    private void selectItemInit(View v) {
        v.findViewById(R.id.hiddenCount).setVisibility(View.VISIBLE);
        v.findViewById(R.id.hiddenMenuLayout).setVisibility(View.VISIBLE);
        seletedItem.findViewById(R.id.coffeeHOT).setSelected(true);
        v.findViewById(R.id.coffeeHOT).setOnClickListener(myListener);
        v.findViewById(R.id.coffeeICED).setOnClickListener(myListener);
        v.findViewById(R.id.countAdd).setOnClickListener(myListener);
        v.findViewById(R.id.countDelete).setOnClickListener(myListener);
        v.findViewById(R.id.cartAdd).setOnClickListener(myListener);

        String[] sizeType = {"Tall", "Grande", "Venti"};
        tallPrice = Integer.parseInt(priceStr);

        Spinner s = (Spinner) v.findViewById(R.id.spinnerSize);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                getContext(),              // 액티비티 클래스 내에 어댑터를 정의할 경우 this는 액티비티 자신을 의미합니다.
                R.layout.spinner_item,     // 현재 선택된 항목을 보여주는 레이아웃의 ID
                sizeType                   // 위에 정의한 문자열의 배열 객체를 대입합니다.
        );

        adapter.setDropDownViewResource(R.layout.dropdown_item);

        final TextView priceText = (TextView) v.findViewById(R.id.priceText);

        s.setAdapter(adapter);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if(parent.getItemAtPosition(position).equals("Tall")) {
                    priceText.setText(String.valueOf(tallPrice) + "원");
                    priceStr = String.valueOf(tallPrice);
                } else if(parent.getItemAtPosition(position).equals("Grande")) {
                    priceText.setText(String.valueOf(tallPrice + 500) + "원");
                    priceStr = String.valueOf(tallPrice + 500);
                } else if(parent.getItemAtPosition(position).equals("Venti")) {
                    priceText.setText(String.valueOf(tallPrice + 1000) + "원");
                    priceStr = String.valueOf(tallPrice + 1000);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * 선택된 아이템에 해당하는 버튼 로직
     */
    View.OnClickListener myListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TextView countText = (TextView)seletedItem.findViewById(R.id.countText);
            int count = Integer.parseInt(countText.getText().toString());
            switch (view.getId()) {
                case R.id.countAdd:
                    count++; // 개수 증가
                    countText.setText(String.valueOf(count));
                    break;
                case R.id.countDelete:
                    if(count>1) { //최소 1 이하로는 떨어지지 않도록 한다
                        count--; // 개수 감소
                    }
                    countText.setText(String.valueOf(count));
                    break;
                case R.id.coffeeHOT:
                    seletedItem.findViewById(R.id.coffeeHOT).setSelected(true);
                    seletedItem.findViewById(R.id.coffeeICED).setSelected(false);
                    break;
                case R.id.coffeeICED :
                    seletedItem.findViewById(R.id.coffeeHOT).setSelected(false);
                    seletedItem.findViewById(R.id.coffeeICED).setSelected(true);
                    break;

                case R.id.cartAdd :
                    String temperature = "HOT";
                    if(seletedItem.findViewById(R.id.coffeeHOT).isSelected()) {
                        temperature = "HOT";
                    } else {
                        temperature = "ICED";
                    }
                    int totalPrice = 0;
                    totalPrice = Integer.parseInt(priceStr) * Integer.parseInt(((TextView) seletedItem.findViewById(R.id.countText)).getText().toString());
                    String text = "name : " + titleStr + ", price : " + totalPrice + ", size : " + ((Spinner) seletedItem.findViewById(R.id.spinnerSize)).getSelectedItem().toString() + ", temperature : " + temperature + ", count : " + ((TextView) seletedItem.findViewById(R.id.countText)).getText().toString();
                    Log.d("text : ", text);
                    break;
            }
        }
    };

}
