//package meow.sweetbread.nekosapikotlin
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//
//class RecycleAdapter (
//    var list: List<String> = listOf()
//): RecyclerView.Adapter<RecycleAdapter.ViewHolder>() {
//
//    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
//        var textView: TextView = itemView.findViewById(R.id.textView)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val itemView = LayoutInflater
//            .from(parent.context)
//            .inflate(R.layout.list_item, parent, false)
//        return ViewHolder(itemView)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.textView.text = list[position]
//    }
//
//    override fun getItemCount(): Int = list.size
//
//}