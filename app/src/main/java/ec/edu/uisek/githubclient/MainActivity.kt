package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val reposAdapter = ReposAdapter(
        onEditClicked = { repo -> launchEditScreen(repo) },
        onDeleteClicked = { repo -> confirmDelete(repo) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        fetchRepositories()
    }

    private fun setupRecyclerView() {
        // Respetamos el GridLayoutManager del XML
        binding.repoRecyclerView.adapter = reposAdapter
    }

    private fun setupFab() {
        binding.newRepoFab.setOnClickListener {
            val intent = Intent(this, RepoForm::class.java)
            startActivity(intent)
        }
    }

    private fun launchEditScreen(repo: Repo) {
        val intent = Intent(this, RepoForm::class.java)
        intent.putExtra("REPO_NAME", repo.name)
        intent.putExtra("REPO_DESC", repo.description)

        repo.owner?.let {
            intent.putExtra("REPO_OWNER", it.login)
            startActivity(intent)
        } ?: showMessage("Error: No se encontró el dueño del repositorio")
    }

    private fun confirmDelete(repo: Repo) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Repositorio")
            .setMessage("¿Estás seguro de eliminar '${repo.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteRepository(repo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun fetchRepositories() {
        RetrofitClient.githubApiService.getRepos().enqueue(object : Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                if (response.isSuccessful) {
                    val repos = response.body() ?: emptyList()
                    reposAdapter.updateRepositories(repos)
                }
            }
            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                Log.e("API", "Fallo al listar: ${t.message}")
            }
        })
    }

    private fun deleteRepository(repo: Repo) {
        val owner = repo.owner?.login ?: return

        RetrofitClient.githubApiService.deleteRepo(owner, repo.name).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 204) {
                    showMessage("Repositorio eliminado")
                    fetchRepositories()
                } else {
                    showMessage("Error al eliminar: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                showMessage("Fallo de conexión")
            }
        })
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}