export default function Loading({ overlay = false }: { overlay?: boolean }) {
    if (overlay) {
        return <div className="fixed inset-0 bg-gray-900 bg-opacity-50 backdrop-blur-sm flex items-center justify-center z-50">
            <div className="text-center bg-white rounded-lg shadow-lg p-6">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                <p className="mt-4 text-gray-600">Loading...</p>
            </div>
        </div>
    }

    return <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">Loading...</p>
        </div>
    </div>
}