package com.example.moneymingle

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moneymingle.ui.theme.MoneyMingleTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.util.*


class MainActivity2 : ComponentActivity() {
    // Initialize Firebase Firestore
    private val db = Firebase.firestore

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val email = intent.getStringExtra("EMAIL") ?: ""
            MoneyMingleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold {
                        Column {
                            Drawer(context = LocalContext.current, email = email)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ATMCard(
    balance: Double,
    expirationDate: Date,
    name: String,
    onTransactionButtonClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var accountNumber by remember { mutableStateOf("") }
    var person by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ),
        ) {
            Column(modifier = Modifier.padding((16.dp))) {
//                Text(
//                    text = "ATM Card",
//                    style = MaterialTheme.typography.headlineSmall
//                )
//                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Balance: $balance",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Expiration Date: ${expirationDate.toString()}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Card Holder:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$name", // Replace with actual card holder name
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Make Transaction")
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Transaction Details") },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Add data to Firestore
                                addToFirestore(accountNumber, person, phone)
                                showDialog = false
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = accountNumber,
                                onValueChange = { accountNumber = it },
                                label = { Text("Account Number") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountBox,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = person,
                                onValueChange = { person = it },
                                label = { Text("Person") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountBox,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

// Function to add data to Firestore
private fun addToFirestore(accountNumber: String, person: String, phone: String) {
    // Create a new user data
    val user = hashMapOf(
        "accountNumber" to accountNumber,
        "person" to person,
        "phone" to phone
    )

    // Add the data to Firestore
    Firebase.firestore.collection("Account")
        .add(user)
        .addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")

        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding document", e)
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Drawer(context: Context, email: String) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Box(modifier = Modifier.background(Color.DarkGray)) {
                ModalDrawerSheet {
                    Column {
                        Text("MoneyMingle", modifier = Modifier
                            .padding(16.dp),
                            fontWeight = FontWeight.Bold)
                    }
                    Divider()
                    val items = listOf("About-App", "Website")
                    var context = LocalContext.current
                    var intent = remember {
                        Intent(context, MainActivity3::class.java)
                    }
                    var intent1 = remember {
                        Intent(context, MainActivity3::class.java)
                    }
                    items.forEach { item ->
                        Row(modifier = Modifier
                            .padding(5.dp, 0.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Icon(imageVector = Icons.Default.AccountBox, contentDescription = "icon")
                            NavigationDrawerItem(
                                label = { Text(text = item) },
                                selected = false,
                                onClick = {
                                    when (item) {
                                        "About-App" -> {
                                            // Handle About-App click
                                            context.startActivity(intent1)
                                        }
                                        "Website" -> {
                                            // Handle Settings-App click
                                            context.startActivity(intent)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) {
        Column {
            Column(modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)) {
                IconButton(onClick = {
                    if (drawerState.isClosed) {
                        scope.launch { drawerState.open() }
                    } else {
                        scope.launch { drawerState.close() }
                    }
                }) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "menu", tint = Color.White)
                }
            }
            ATMCard(
                balance = 1000.0,
                expirationDate = Date(),
                name = email,
                onTransactionButtonClick = {
                    // Handle transaction button click
                }
            )
            Row(modifier =Modifier.align(Alignment.CenterHorizontally) ){
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TransactionList()
        }
    }
}

// Data class to represent a transaction
data class Transaction(
    val id: String,
    val accountNumber: String,
    val person: String,
    val phone: String
)

@Composable
fun TransactionList() {
    // Mutable state to hold the list of transactions
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    // Fetch transactions from Firestore when this composable is first composed
    LaunchedEffect(Unit) {
        fetchTransactions { fetchedTransactions ->
            transactions = fetchedTransactions
        }
    }

    // LazyColumn to display the list of transactions
    LazyColumn {
        items(transactions) { transaction ->
//            Text(text = "Transaction ID: ${transaction.id}")
            Column (modifier = Modifier.padding(10.dp).clickable { showMenu=true  }){
                Text(text = "Account Number: ${transaction.accountNumber}")
                Text(text = "Person: ${transaction.person}")
                Text(text = "Phone: ${transaction.phone}")
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu=false }) {
                DropdownMenuItem(text = { Text(text = "${transaction.id}")}, onClick = {
                    Toast.makeText(context, "Name: ${transaction.id}", Toast.LENGTH_SHORT).show()
                    showMenu = false
                })
            }
            Divider()
        }
    }
}

// Function to fetch transactions from Firestore
private fun fetchTransactions(onSuccess: (List<Transaction>) -> Unit) {
    Firebase.firestore.collection("Account")
        .get()
        .addOnSuccessListener { result ->
            val transactions = mutableListOf<Transaction>()
            for (document in result) {
                val transaction = Transaction(
                    id = document.id,
                    accountNumber = document.getString("accountNumber") ?: "",
                    person = document.getString("person") ?: "",
                    phone = document.getString("phone") ?: ""
                )
                transactions.add(transaction)
            }
            // Call the onSuccess callback with the list of transactions
            onSuccess(transactions)
        }
        .addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents.", exception)
        }
}