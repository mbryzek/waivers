module Page.Admin exposing (view)

import Html exposing (Html, a, div, h1, h2, p, text)
import Html.Attributes exposing (class, href)
import Templates.Shell as Shell


view : Maybe Int -> Html msg
view currentYear =
    Shell.view
        { title = "Admin Dashboard"
        , currentYear = currentYear
        , content =
            [ div [ class "max-w-4xl mx-auto" ]
                [ h1 [ class "text-3xl font-bold text-gray-900 mb-6" ]
                    [ text "Admin Dashboard" ]
                , div [ class "grid md:grid-cols-2 lg:grid-cols-3 gap-6" ]
                    [ div [ class "bg-white p-6 rounded-lg shadow-md" ]
                        [ h2 [ class "text-xl font-semibold text-gray-900 mb-3" ]
                            [ text "Projects" ]
                        , p [ class "text-gray-600 mb-4" ]
                            [ text "Manage waiver projects and their settings." ]
                        , a [ href "/admin/projects", class "inline-block bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700" ]
                            [ text "Manage Projects" ]
                        ]
                    , div [ class "bg-white p-6 rounded-lg shadow-md" ]
                        [ h2 [ class "text-xl font-semibold text-gray-900 mb-3" ]
                            [ text "Signatures" ]
                        , p [ class "text-gray-600 mb-4" ]
                            [ text "View and export signed waivers." ]
                        , a [ href "/admin/signatures", class "inline-block bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700" ]
                            [ text "View Signatures" ]
                        ]
                    , div [ class "bg-white p-6 rounded-lg shadow-md" ]
                        [ h2 [ class "text-xl font-semibold text-gray-900 mb-3" ]
                            [ text "Reports" ]
                        , p [ class "text-gray-600 mb-4" ]
                            [ text "Generate reports and analytics." ]
                        , a [ href "#", class "inline-block bg-purple-600 text-white px-4 py-2 rounded-md hover:bg-purple-700" ]
                            [ text "Generate Reports" ]
                        ]
                    ]
                ]
            ]
        }
