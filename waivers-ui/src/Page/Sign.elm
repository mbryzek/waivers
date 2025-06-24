module Page.Sign exposing (Model, Msg(..), init, update, view)

import Constants
import Generated.ApiRequest as ApiRequest exposing (ApiRequest(..))
import Generated.IoBryzekWaiversApi as Api
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Svg
import Svg.Attributes as SvgAttr
import Url


type alias Model =
    { signatureId : String
    , pdfUrl : Maybe String
    , signatureData : String
    , signatureRequest : ApiRequest Api.Signature
    }


type Msg
    = SignatureDataChanged String
    | SubmitSignature
    | SignatureSubmitted (ApiRequest.ApiResult Api.Signature)
    | RetrySignature


init : String -> Url.Url -> Model
init signatureId url =
    let
        pdfUrl =
            url.query
                |> Maybe.map
                    (\queryString ->
                        queryString
                            |> String.split "&"
                            |> List.filterMap
                                (\param ->
                                    case String.split "=" param of
                                        [ "pdf", encodedUrl ] ->
                                            Just (Maybe.withDefault encodedUrl (Url.percentDecode encodedUrl))

                                        _ ->
                                            Nothing
                                )
                            |> List.head
                    )
                |> Maybe.withDefault Nothing
    in
    { signatureId = signatureId
    , pdfUrl = pdfUrl
    , signatureData = ""
    , signatureRequest = NotAsked
    }


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        SignatureDataChanged data ->
            ( { model | signatureData = data }, Cmd.none )

        SubmitSignature ->
            if String.isEmpty (String.trim model.signatureData) then
                ( model, Cmd.none )

            else
                ( { model | signatureRequest = Loading }
                , submitSignature model.signatureId model.signatureData
                )

        SignatureSubmitted result ->
            ( { model | signatureRequest = ApiRequest.fromResult result }, Cmd.none )

        RetrySignature ->
            ( { model | signatureRequest = NotAsked }, Cmd.none )


submitSignature : String -> String -> Cmd Msg
submitSignature signatureId signatureData =
    let
        signatureCompletion =
            Api.SignatureCompletion signatureData

        httpParams =
            Api.HttpRequestParams Constants.apiHost []
    in
    Api.postSignaturesSignaturesCompleteById signatureId signatureCompletion SignatureSubmitted httpParams


view : Model -> Html Msg
view model =
    div [ class "min-h-screen bg-gray-50 py-8" ]
        [ div [ class "max-w-4xl mx-auto px-4 sm:px-6 lg:px-8" ]
            [ div [ class "bg-white rounded-lg shadow-lg overflow-hidden" ]
                [ div [ class "px-6 py-8 border-b border-gray-200" ]
                    [ h1 [ class "text-2xl font-bold text-gray-900" ]
                        [ text "Sign Your Waiver" ]
                    , p [ class "mt-2 text-gray-600" ]
                        [ text "Please review the document below and provide your signature to complete the waiver." ]
                    ]
                , div [ class "p-6" ]
                    [ case model.signatureRequest of
                        Success signature ->
                            viewSuccess signature model

                        _ ->
                            viewSigningForm model
                    ]
                ]
            ]
        ]


viewSuccess : Api.Signature -> Model -> Html Msg
viewSuccess signature model =
    div [ class "text-center py-12" ]
        [ div [ class "mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100" ]
            [ Svg.svg [ class "h-6 w-6 text-green-600", SvgAttr.fill "none", SvgAttr.viewBox "0 0 24 24", SvgAttr.stroke "currentColor" ]
                [ Svg.path [ SvgAttr.strokeLinecap "round", SvgAttr.strokeLinejoin "round", SvgAttr.strokeWidth "2", SvgAttr.d "M5 13l4 4L19 7" ] [] ]
            ]
        , h3 [ class "mt-4 text-lg font-medium text-gray-900" ]
            [ text "Waiver Signed Successfully!" ]
        , p [ class "mt-2 text-gray-500" ]
            [ text "Thank you for completing your waiver. You will receive a copy via email shortly." ]
        , case model.pdfUrl of
            Just pdfUrl ->
                div [ class "mt-6" ]
                    [ a
                        [ href pdfUrl
                        , download ("waiver-" ++ signature.id ++ ".pdf")
                        , class "inline-flex items-center px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-md shadow-sm transition-colors"
                        ]
                        [ Svg.svg [ class "w-5 h-5 mr-2", SvgAttr.fill "none", SvgAttr.viewBox "0 0 24 24", SvgAttr.stroke "currentColor" ]
                            [ Svg.path [ SvgAttr.strokeLinecap "round", SvgAttr.strokeLinejoin "round", SvgAttr.strokeWidth "2", SvgAttr.d "M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" ] [] ]
                        , text "Download Signed Waiver"
                        ]
                    ]

            Nothing ->
                text ""
        ]


viewSigningForm : Model -> Html Msg
viewSigningForm model =
    div [ class "space-y-6" ]
        [ -- PDF Viewer Section
          case model.pdfUrl of
            Just pdfUrl ->
                div []
                    [ h3 [ class "text-lg font-medium text-gray-900 mb-4" ]
                        [ text "Waiver Document" ]
                    , div [ class "border border-gray-300 rounded-lg overflow-hidden" ]
                        [ iframe
                            [ src pdfUrl
                            , class "w-full h-96"
                            , title "Waiver Document"
                            , id "waiver-pdf-iframe"
                            ]
                            []
                        ]
                    ]

            Nothing ->
                div [ class "bg-yellow-50 border border-yellow-200 rounded-lg p-4" ]
                    [ p [ class "text-yellow-800" ]
                        [ text "PDF document could not be loaded. Please contact support if this issue persists." ]
                    ]

        -- Signature Section
        , case model.signatureRequest of
            Loading ->
                -- Show submitting state with no interactive elements
                div [ class "text-center py-8" ]
                    [ h3 [ class "text-lg font-medium text-gray-900 mb-4" ]
                        [ text "Processing your signature..." ]
                    , div [ class "inline-flex items-center" ]
                        [ div [ class "animate-spin -ml-1 mr-3 h-5 w-5 text-blue-600" ]
                            [ div [ class "rounded-full h-4 w-4 border-t-2 border-b-2 border-blue-600" ] [] ]
                        , span [ class "text-gray-600" ] [ text "Please wait while we process your signature" ]
                        ]
                    ]

            Failure apiError ->
                div [ class "space-y-4" ]
                    [ div [ class "bg-red-50 border border-red-200 rounded-lg p-4" ]
                        [ div [ class "flex" ]
                            [ div [ class "flex-shrink-0" ]
                                [ Svg.svg [ class "h-5 w-5 text-red-400", SvgAttr.viewBox "0 0 20 20", SvgAttr.fill "currentColor" ]
                                    [ Svg.path [ SvgAttr.fillRule "evenodd", SvgAttr.d "M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z", SvgAttr.clipRule "evenodd" ] [] ]
                                ]
                            , div [ class "ml-3" ]
                                [ h3 [ class "text-sm font-medium text-red-800" ]
                                    [ text "Error submitting signature" ]
                                , div [ class "mt-2 text-sm text-red-700" ]
                                    [ p [] [ text (apiErrorToString apiError) ]
                                    , p [ class "mt-2" ] [ text "Please check your internet connection and try again." ]
                                    ]
                                , div [ class "mt-4" ]
                                    [ button
                                        [ type_ "button"
                                        , class "text-sm bg-red-100 hover:bg-red-200 text-red-800 px-3 py-1 rounded border border-red-300"
                                        , onClick RetrySignature
                                        ]
                                        [ text "Try Again" ]
                                    ]
                                ]
                            ]
                        ]
                    , viewSignatureForm model
                    ]

            _ ->
                viewSignatureForm model
        ]


apiErrorToString : ApiRequest.ApiError -> String
apiErrorToString error =
    case error of
        ApiRequest.ApiErrorSystem msg ->
            if String.contains "NetworkError" msg then
                "Unable to connect to the server. Please check your internet connection or try again later."

            else if String.contains "Timeout" msg then
                "Request timed out. Please check your connection and try again."

            else
                "System error: " ++ msg

        ApiRequest.ApiErrorNotFound ->
            "Signature not found. This signature may have expired or been removed."

        ApiRequest.ApiErrorNotAuthorized ->
            "Not authorized to access this signature."

        ApiRequest.ApiErrorUnsupportedStatusCode code ->
            if code == 500 then
                "Server error. Please try again in a few moments."

            else if code == 404 then
                "Signature endpoint not found. Please contact support."

            else
                "HTTP Error " ++ String.fromInt code ++ ". Please try again."

        ApiRequest.ApiErrorJsonParse _ ->
            "Failed to parse server response. Please try again or contact support."

        ApiRequest.ApiErrorValidation errors ->
            "Validation errors: " ++ String.join ", " (List.map .message errors)


viewSignatureForm : Model -> Html Msg
viewSignatureForm model =
    div []
        [ h3 [ class "text-lg font-medium text-gray-900 mb-4" ]
            [ text "Your Signature" ]
        , div [ class "space-y-4" ]
            [ div []
                [ label [ for "signature", class "block text-sm font-medium text-gray-700" ]
                    [ text "Type your full name to sign" ]
                , input
                    [ type_ "text"
                    , id "signature"
                    , class "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    , placeholder "Enter your full name"
                    , value model.signatureData
                    , onInput SignatureDataChanged
                    ]
                    []
                ]
            , div []
                [ button
                    [ type_ "button"
                    , class "w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                    , onClick SubmitSignature
                    , disabled (String.isEmpty (String.trim model.signatureData))
                    ]
                    [ text "Sign Waiver" ]
                ]
            ]
        ]
