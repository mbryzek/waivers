module Templates.Forms exposing (textInput, textArea, button, formGroup)

import Html exposing (Html, div, input, textarea, text, label)
import Html.Attributes as Attr exposing (class, type_, value, placeholder, id, for, rows)
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


textArea : { value : String, placeholder : String, onInput : String -> msg, rows : Int } -> Html msg
textArea { value, placeholder, onInput, rows } =
    textarea
        [ Attr.value value
        , Attr.placeholder placeholder
        , Html.Events.onInput onInput
        , Attr.rows rows
        , class "w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-vertical"
        ]
        []


button : { text : String, onClick : msg, primary : Bool } -> Html msg
button { text, onClick, primary } =
    Html.button
        [ Html.Events.onClick onClick
        , class <|
            if primary then
                "px-4 py-2 bg-blue-600 text-white font-medium rounded-md shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"

            else
                "px-4 py-2 bg-white text-gray-700 font-medium rounded-md border border-gray-300 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        ]
        [ Html.text text ]