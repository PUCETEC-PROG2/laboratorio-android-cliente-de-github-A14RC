package ec.edu.uisek.githubclient

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoForm : AppCompatActivity() {

    private lateinit var binding: ActivityRepoFormBinding

    // Variables para controlar si estamos editando
    private var isEditMode = false
    private var currentRepoOwner: String? = null
    private var currentRepoName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Primero revisamos si nos enviaron datos para editar
        checkIntentData()

        // 2. Configuramos los botones
        setupListeners()
    }

    private fun checkIntentData() {
        // Intentamos leer datos que podrían haber sido enviados desde el MainActivity
        val name = intent.getStringExtra("REPO_NAME")
        val desc = intent.getStringExtra("REPO_DESC")
        val owner = intent.getStringExtra("REPO_OWNER")

        if (name != null && owner != null) {
            // Si llegaron datos, estamos en MODO EDICIÓN!
            isEditMode = true
            currentRepoName = name
            currentRepoOwner = owner

            // Rellenamos los campos
            binding.repoNameInput.setText(name)
            binding.repoDescriptionInput.setText(desc)

            // Cambiamos el texto del botón
            binding.saveButton.text = "Actualizar Repositorio"

            // REQUISITO DEL LAB: El nombre no debe ser editable
            binding.repoNameInput.isEnabled = false
        }
    }

    private fun setupListeners() {
        binding.cancelButton.setOnClickListener { finish() }

        binding.saveButton.setOnClickListener {
            if (isEditMode) {
                updateRepo()
            } else {
                createRepo()
            }
        }
    }

    // Lógica para CREAR (POST)
    private fun createRepo() {
        val name = binding.repoNameInput.text.toString()
        val desc = binding.repoDescriptionInput.text.toString()

        if (name.isEmpty()) {
            binding.repoNameInput.error = "El nombre es obligatorio"
            return
        }

        val request = RepoRequest(name, desc)

        RetrofitClient.githubApiService.createRepo(request).enqueue(object : Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio creado con éxito")
                    finish()
                } else {
                    showMessage("Error al crear: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Repo>, t: Throwable) {
                showMessage("Error de red: ${t.message}")
            }
        })
    }

    // Lógica para ACTUALIZAR (PATCH)
    private fun updateRepo() {
        val desc = binding.repoDescriptionInput.text.toString()

        // Recuperamos los datos seguros que guardamos al inicio
        val name = currentRepoName ?: return
        val owner = currentRepoOwner ?: return

        val request = RepoRequest(name, desc)

        // Llamamos al método updateRepo que creamos en el Paso 3
        RetrofitClient.githubApiService.updateRepo(owner, name, request).enqueue(object : Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio actualizado")
                    finish()
                } else {
                    showMessage("Error al actualizar: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Repo>, t: Throwable) {
                showMessage("Error de red: ${t.message}")
            }
        })
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}