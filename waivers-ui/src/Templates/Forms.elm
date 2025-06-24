module Templates.Forms exposing (formGroup, textInput)

import Html exposing (Html, div, input, label, text)
import Html.Attributes as Attr exposing (class, type_)
import Html.Events


formGroup : String -> Html msg -> Html msg
formGroup labelText inputElement =
    div [ class "mb-4" ]
        [ label [ class "block text-sm font-medium text-gray-700 mb-2" ]
            [ text labelText ]
        , inputElement
        ]


textInput : { value : String, placeholder : String, onInput : String -> msg } -> Html msg
textInput { value, placeholder, onInput } =
    input
        [ type_ "text"
        , Attr.value value
        , Attr.placeholder placeholder
        , Html.Events.onInput onInput
        , class "w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        ]
        []
