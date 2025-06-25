module Page.Index exposing (view)

import Html exposing (Html, a, div, h1, h2, p, text)
import Html.Attributes exposing (class, href)
import Templates.Shell as Shell


view : Maybe Int -> Html msg
view currentYear =
    Shell.view
        { title = "Welcome to Waivers"
        , currentYear = currentYear
        , content =
            [ div [ class "max-w-4xl mx-auto" ]
                [ h1 [ class "text-4xl font-bold text-gray-900 mb-6" ]
                    [ text "Digital Waiver Signing System" ]
                , p [ class "text-xl text-gray-600 mb-8" ]
                    [ text "Create, manage, and collect legally binding digital signatures on waivers for your organization." ]
                , div [ class "grid md:grid-cols-2 gap-8" ]
                    [ div [ class "bg-white p-6 rounded-lg shadow-md" ]
                        [ h2 [ class "text-2xl font-semibold text-gray-900 mb-4" ]
                            [ text "For Participants" ]
                        , p [ class "text-gray-600 mb-4" ]
                            [ text "Sign your waiver digitally with HelloSign integration for legally binding signatures." ]
                        , a [ href "/waiver/pickleball", class "inline-block bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700" ]
                            [ text "Try Pickleball Waiver" ]
                        ]
                    , div [ class "bg-white p-6 rounded-lg shadow-md" ]
                        [ h2 [ class "text-2xl font-semibold text-gray-900 mb-4" ]
                            [ text "For Administrators" ]
                        , p [ class "text-gray-600 mb-4" ]
                            [ text "Manage waiver projects, view signatures, and export data for your organization." ]
                        , a [ href "/admin", class "inline-block bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700" ]
                            [ text "Admin Dashboard" ]
                        ]
                    ]
                ]
            ]
        }
