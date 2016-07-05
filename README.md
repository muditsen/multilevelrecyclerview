#MultiLevel Expandable RecyclerView.


This is extension of recycler view to make it behave has expandable list view.


##Prerequisites

add this 
```
allprojects {
    repositories {
        jcenter()
        maven {
            url 'https://dl.bintray.com/iammuditsen/maven/'
        }
    }
}
```

to app gradle file and in dependencies add
Note: above do not allprojects tag in any parent tag.
```
 compile 'muditse.android:multilevelexpandview:1.0'
 ```
OR
You need to include `multilevelview` module into your project after downloading it.


##Usage
```
<com.multilevelview.MultiLevelRecyclerView
        android:id="@+id/rv_list"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
/>
```
In your layout file.


And declare your RecyclerView in your Activity or Fragment like

```

        MultiLevelRecyclerView multiLevelRecyclerView = (MultiLevelRecyclerView) findViewById(R.id.rv_list);

        multiLevelRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Item> itemList = new ArrayList<>();
        for(int i=0;i<10;i++){
            Item item = new Item(1);
            List<RecyclerViewItem> itemList2 = new ArrayList<>();
            for(int j=0;j<5;j++){
                Item item2 = new Item(2);
                List<RecyclerViewItem> itemList3 = new ArrayList<>();
                for(int k=0;k<3;k++){
                    Item item3 = new Item(3);
                    item3.setText("XYZ "+k);
                    item3.setSecondText("xyz "+k);
                    itemList3.add(item3);
                }
                item2.setText("PQRST "+j);
                item2.setSecondText("pqrst "+j);
                item2.addChildren(itemList3);
                itemList2.add(item2);
            }
            item.setText("ABCDE "+i);
            item.setSecondText("abcde "+i);
            item.addChildren(itemList2);
            itemList.add(item);
        }
        MyAdapter myAdapter = new MyAdapter(itemList);

multiLevelRecyclerView.setAdapter(myAdapter);
```

And last your Adapter file must be extending to `MultiLevelAdapter.java`.


Example code is already included.

#License

Copyright (C) 2015 Mudti Sen <muditsen1234@gmail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


