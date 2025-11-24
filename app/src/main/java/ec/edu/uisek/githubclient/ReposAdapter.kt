package ec.edu.uisek.githubclient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ec.edu.uisek.githubclient.databinding.FragmentRepoItemBinding
import ec.edu.uisek.githubclient.models.Repo

// El adaptador ahora recibe funciones para saber qué hacer al editar/borrar
class ReposAdapter(
    private val onEditClicked: (Repo) -> Unit,
    private val onDeleteClicked: (Repo) -> Unit
) : RecyclerView.Adapter<RepoViewHolder>() {

    private var repos: List<Repo> = emptyList()

    fun updateRepositories(newRepos: List<Repo>) {
        this.repos = newRepos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
        val binding = FragmentRepoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RepoViewHolder(binding)
    }

    override fun getItemCount(): Int = repos.size

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        holder.bind(repos[position], onEditClicked, onDeleteClicked)
    }
}

class RepoViewHolder(private val binding: FragmentRepoItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(repo: Repo, onEdit: (Repo) -> Unit, onDelete: (Repo) -> Unit) {
        binding.repoName.text = repo.name
        binding.repoDescription.text = repo.description ?: "Sin descripción"

        // Configurar los clics de los botones de colores
        binding.btnEdit.setOnClickListener { onEdit(repo) }
        binding.btnDelete.setOnClickListener { onDelete(repo) }
    }
}