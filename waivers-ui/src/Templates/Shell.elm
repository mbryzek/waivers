module Templates.Shell exposing (view)

import Html exposing (Html, a, div, h1, nav, p, text)
import Html.Attributes exposing (class, href)


view : { title : String, content : List (Html msg), currentYear : Maybe Int } -> Html msg
view { content, currentYear } =
    div [ class "min-h-screen bg-gray-50" ]
        [ viewHeader
        , div [ class "container mx-auto px-4 py-8" ]
            content
        , viewFooter currentYear
        ]


viewHeader : Html msg
viewHeader =
    nav [ class "bg-white shadow-sm border-b border-gray-200" ]
        [ div [ class "container mx-auto px-4" ]
            [ div [ class "flex justify-between items-center h-16" ]
                [ div [ class "flex items-center" ]
                    [ h1 [ class "text-xl font-semibold text-gray-900" ]
                        [ a [ href "/", class "hover:text-blue-600" ]
                            [ text "Waivers" ]
                        ]
                    ]
                , div [ class "flex items-center space-x-6" ]
                    [ a [ href "/admin", class "text-gray-600 hover:text-gray-900" ]
                        [ text "Admin" ]
                    ]
                ]
            ]
        ]


viewFooter : Maybe Int -> Html msg
viewFooter currentYear =
    let
        year : String
        year =
            case currentYear of
                Just yr ->
                    String.fromInt yr

                Nothing ->
                    "2025"
    in
    div [ class "bg-gray-100 border-t border-gray-200 py-6 mt-16" ]
        [ div [ class "container mx-auto px-4 text-center" ]
            [ p [ class "text-sm text-gray-600" ]
                [ text ("© " ++ year ++ " Lake View Summit LLC. All rights reserved.") ]
            ]
        ]
