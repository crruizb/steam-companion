export default function Loading({ overlay = false }: { overlay?: boolean }) {
    if (overlay) {
        return <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
            <div className="text-center bg-card border border-line/60 rounded-2xl shadow-lg p-6">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent mx-auto"></div>
                <p className="mt-4 text-muted">Loading...</p>
            </div>
        </div>
    }

    return <div className="min-h-screen bg-page flex items-center justify-center">
        <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent mx-auto"></div>
            <p className="mt-4 text-muted">Loading...</p>
        </div>
    </div>
}