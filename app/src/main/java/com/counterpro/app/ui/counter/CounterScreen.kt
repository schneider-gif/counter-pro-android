package com.counterpro.app.ui.counter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CounterScreen(vm: CounterViewModel) {
    val count by vm.count.collectAsState()
    val label by vm.label.collectAsState()
    var editingLabel by remember { mutableStateOf(false) }
    var labelInput by remember { mutableStateOf(label) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (editingLabel) {
            OutlinedTextField(
                value = labelInput,
                onValueChange = { labelInput = it },
                label = { Text("Rótulo") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    vm.setLabel(labelInput)
                    editingLabel = false
                }),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            TextButton(onClick = { editingLabel = true; labelInput = label }) {
                Text(label, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = count.toString(),
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FilledTonalButton(
                onClick = { vm.decrement() },
                modifier = Modifier.size(80.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("−", fontSize = 36.sp)
            }
            Button(
                onClick = { vm.increment() },
                modifier = Modifier.size(80.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("+", fontSize = 36.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { vm.reset() }) { Text("Zerar") }
            Button(onClick = { vm.save() }) { Text("Salvar") }
        }
    }
}
