import SwiftUI

/// Standalone account panel — also embedded in the list banner.
struct AccountStatusView: View {
    @Environment(NoteRepository.self) private var repository

    var body: some View {
        List {
            Section("iCloud") {
                Label(repository.accountStatus.title, systemImage: repository.accountStatus.systemImage)
                LabeledContent("Active mode", value: repository.activeMode.rawValue)
                if let message = repository.syncMessage {
                    Text(message).font(.footnote).foregroundStyle(.secondary)
                }
            }
            Section("Actions") {
                Button("Refresh account status") {
                    Task { await repository.refreshAccountAndMode() }
                }
                Button("Sync now") {
                    Task { await repository.syncNow() }
                }
                Button("Force local fallback", role: .destructive) {
                    repository.forceLocalMode()
                }
            }
            Section("Conflict policy") {
                Text("When the same note changes in two places, Sync Notes keeps the higher `revision`. If revisions tie, the later `updatedAt` wins (last-writer-wins). Soft-deleted notes tombstone via `isDeleted` before local removal.")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
        }
        .navigationTitle("Account")
    }
}
