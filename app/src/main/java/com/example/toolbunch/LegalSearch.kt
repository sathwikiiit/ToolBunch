package com.example.toolbunch

import android.content.Context
import androidx.compose.material3.* // Import ALL Material 3 components
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.FileOutputStream
import java.io.InputStream


@Composable
fun LegalSearch() {
    val context = LocalContext.current
    var dbHelper: DbHelper? = null
    var dbError by remember { mutableStateOf(false) }

    dbHelper = remember { DbHelper(context) }

    var searchText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var selectedTable by remember { mutableStateOf("IPC") }

    val tableOptions = listOf("IPC", "CRPC", "CPC", "MVA", "IDA", "HMA", "IEA", "NIA")

    LaunchedEffect(searchText, selectedTable) {
        if (searchText.isNotBlank() && !dbError) {
            isLoading = true
            scope.launch(Dispatchers.IO) {
                searchResults = dbHelper.searchText(searchText, selectedTable)
                isLoading = false
            }
        } else {
            searchResults = emptyList()
        }
    }

    LegalSearchContent(
        searchText = searchText,
        onSearchTextChanged = { searchText = it },
        searchResults = searchResults,
        isLoading = isLoading,
        dbError = dbError,
        selectedTable = selectedTable,
        onTableSelected = { selectedTable = it },
        tableOptions = tableOptions
    )
}

@Composable
fun LegalSearchContent(
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    searchResults: List<SearchResult>,
    isLoading: Boolean,
    dbError: Boolean,
    selectedTable: String,
    onTableSelected: (String) -> Unit,
    tableOptions: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var expanded by remember { mutableStateOf(false) }

        OutlinedTextField(
            readOnly = true,
            value = selectedTable,
            onValueChange = {},
            label = { Text("Select Table") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            textStyle = TextStyle(color = Color.Black)
        )

        // Wrap the DropdownMenu in a Box with a placement strategy
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { // Match TextField width and add padding
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                // Use offset to align with the TextField's start
                offset = DpOffset(x = 0.dp, y = 5.dp), // Adjust vertical offset as needed
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                tableOptions.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            onTableSelected(option)
                            expanded = false
                        },
                        text = { Text(option) }
                    )
                }
            }
        }


        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChanged,
            label = { Text("Search in $selectedTable") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), // Add horizontal padding
            singleLine = true,
            shape = RoundedCornerShape(8.dp), // Rounded corners
            textStyle = TextStyle(color = Color.Black)

        )

        Spacer(modifier = Modifier.height(16.dp))

        if (dbError) {
            Text("Error loading database. Search is unavailable.", color = MaterialTheme.colorScheme.error)
        } else if (isLoading) {
            CircularProgressIndicator()
        } else if (searchResults.isEmpty() && searchText.isNotBlank()) {
            Text("No results found in $selectedTable.")
        } else {
            LazyColumn {
                items(searchResults) { result ->
                    SearchResultItem(result)
                }
            }
        }
    }
}





@Composable
fun SearchResultItem(result: SearchResult) {
    var isExpanded by remember { mutableStateOf(false) }
    result.sectionTitle=result.sectionTitle.replace("\n\n","")
    result.sectionDesc=result.sectionDesc.replace("\n\n","")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Section: ${result.section} - ${result.sectionTitle}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Expand" else "Collapse"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isExpanded) result.sectionDesc else {
                    if (result.sectionDesc.length > 100) {
                        result.sectionDesc.substring(0, 100) + "..."
                    } else {
                        result.sectionDesc
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "IndiaLaw.db"
        const val DATABASE_VERSION = 1

        const val COLUMN_SECTION = "section"
        const val COLUMN_SECTION_TITLE = "section_title"
        const val COLUMN_SECTION_DESC = "section_desc"
    }

    init {
        try {
            copyDatabase(context)
        } catch (e: IOException) {
            throw IllegalStateException("Error copying database: ${e.message}")
        }
    }

    private fun copyDatabase(context: Context) {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        if (dbFile.exists()) {
            return
        }

        try {
            val inputStream: InputStream = context.assets.open(DATABASE_NAME)
            val outputStream = FileOutputStream(dbFile)

            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error copying database", Toast.LENGTH_SHORT).show()
            throw e
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Leave empty if the database file is already created and populated in assets.
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle upgrades if needed.  If your table structure changes, handle it here.
    }

    fun searchText(searchText: String, tableName: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val db = readableDatabase

        try {
            val keywords = searchText.trim().split("\\s+".toRegex())
            val whereClause = StringBuilder()
            val selectionArgs = mutableListOf<String>()

            keywords.forEach { keyword ->
                if (whereClause.isNotEmpty()) {
                    whereClause.append(" OR ")
                }
                whereClause.append("$COLUMN_SECTION_TITLE LIKE ? OR $COLUMN_SECTION_DESC LIKE ? OR $COLUMN_SECTION LIKE ?")
                selectionArgs.add("%$keyword%")
                selectionArgs.add("%$keyword%")
                selectionArgs.add("%$keyword%")
            }

            val cursor: Cursor? = db.query(
                tableName,
                arrayOf(COLUMN_SECTION, COLUMN_SECTION_TITLE, COLUMN_SECTION_DESC),
                whereClause.toString(),
                selectionArgs.toTypedArray(),
                null,
                null,
                null
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val section = it.getString(it.getColumnIndexOrThrow(COLUMN_SECTION))
                    val sectionTitle = it.getString(it.getColumnIndexOrThrow(COLUMN_SECTION_TITLE))
                    val sectionDesc = it.getString(it.getColumnIndexOrThrow(COLUMN_SECTION_DESC))

                    val searchResult = SearchResult(section, sectionTitle, sectionDesc)
                    val score = calculateRelevance(sectionTitle, sectionDesc, section,keywords,searchText)
                    searchResult.relevance = score
                    results.add(searchResult)
                }
            }

            results.sortByDescending { it.relevance }

        } catch (e: Exception) {
            println("Error searching: ${e.message}")
            return emptyList()
        } finally {
            db.close()
        }

        return results
    }

    private fun calculateRelevance(
        title: String,
        description: String,
        section: String,
        keywords: List<String>,
        searchText: String
    ): Int {
        var score = 0
        keywords.forEach { keyword ->
            if (title.contains(keyword, ignoreCase = true)) {
                score += 2
            }
            if (description.contains(keyword, ignoreCase = true)) {
                score += 1
            }
            if (section.contains(searchText, ignoreCase = true)) {
                score += 10
            }
        }
        return score
    }
}

data class SearchResult(
    val section: String,
    var sectionTitle: String,
    var sectionDesc: String,
    var relevance: Int = 0
)