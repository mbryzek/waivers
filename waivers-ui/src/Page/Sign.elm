module Page.Sign exposing (Model, Msg, init, update, view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Svg
import Svg.Attributes as SvgAttr
import Generated.IoBryzekWaiversApi as Api
import Generated.ApiRequest
import Json.Decode as Decode
import Json.Encode as Encode
import Http
import Url
import Url.Parser as Parser
import Url.Parser.Query as Query


type alias Model =
    { signatureId : String
    , pdfUrl : Maybe String
    , signatureData : String
    , isSubmitting : Bool
    , error : Maybe String
    , success : Bool
    }


type Msg
    = SignatureDataChanged String
    | SubmitSignature
    | SignatureSubmitted (Result Http.Error Api.Signature)


init : String -> Url.Url -> Model
init signatureId url =
    let
        pdfUrl = 
            url.query
                |> Maybe.map (\queryString -> 
                    queryString
                        |> String.split "&"
                        |> List.filterMap (\param ->
                            case String.split "=" param of
                                ["pdf", encodedUrl] -> 
                                    Just (Maybe.withDefault encodedUrl (Url.percentDecode encodedUrl))
                                _ -> Nothing
                        )
                        |> List.head
                )
                |> Maybe.withDefault Nothing
    in
    { signatureId = signatureId
    , pdfUrl = pdfUrl
    , signatureData = ""
    , isSubmitting = False
    , error = Nothing
    , success = False
    }


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        SignatureDataChanged data ->
            ( { model | signatureData = data }, Cmd.none )
        
        SubmitSignature ->
            if String.isEmpty (String.trim model.signatureData) then
                ( { model | error = Just "Please enter your signature" }, Cmd.none )
            else
                ( { model | isSubmitting = True, error = Nothing }
                , submitSignature model.signatureId model.signatureData
                )
        
        SignatureSubmitted (Ok signature) ->
            ( { model | isSubmitting = False, success = True }, Cmd.none )
        
        SignatureSubmitted (Err error) ->
            let
                errorMessage = 
                    case error of
                        Http.BadStatus 404 ->
                            "Signature not found"
                        Http.BadStatus 400 ->
                            "Invalid signature data"
                        _ ->
                            "Failed to submit signature"
            in
            ( { model | isSubmitting = False, error = Just errorMessage }, Cmd.none )


submitSignature : String -> String -> Cmd Msg
submitSignature signatureId signatureData =
    let
        body = 
            Encode.object
                [ ( "signature_data", Encode.string signatureData ) ]
    in
    Http.request
        { method = "POST"
        , url = "http://localhost:9300/signatures/" ++ signatureId ++ "/complete"
        , expect = Http.expectJson SignatureSubmitted Api.signatureDecoder
        , headers = [ Http.header "Content-Type" "application/json" ]
        , timeout = Nothing
        , tracker = Nothing
        , body = Http.jsonBody body
        }


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
                    [ if model.success then
                        viewSuccess
                      else
                        viewSigningForm model
                    ]
                ]
            ]
        ]


viewSuccess : Html Msg
viewSuccess =
    div [ class "text-center py-12" ]
        [ div [ class "mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100" ]
            [ Svg.svg [ class "h-6 w-6 text-green-600", SvgAttr.fill "none", SvgAttr.viewBox "0 0 24 24", SvgAttr.stroke "currentColor" ]
                [ Svg.path [ SvgAttr.strokeLinecap "round", SvgAttr.strokeLinejoin "round", SvgAttr.strokeWidth "2", SvgAttr.d "M5 13l4 4L19 7" ] [] ]
            ]
        , h3 [ class "mt-4 text-lg font-medium text-gray-900" ]
            [ text "Waiver Signed Successfully!" ]
        , p [ class "mt-2 text-gray-500" ]
            [ text "Thank you for completing your waiver. You will receive a copy via email shortly." ]
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
        , div []
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
                        , disabled model.isSubmitting
                        ]
                        []
                    ]
                
                , case model.error of
                    Just error ->
                        div [ class "text-red-600 text-sm" ]
                            [ text error ]
                    
                    Nothing ->
                        text ""
                
                , div []
                    [ button
                        [ type_ "button"
                        , class "w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                        , onClick SubmitSignature
                        , disabled (model.isSubmitting || String.isEmpty (String.trim model.signatureData))
                        ]
                        [ if model.isSubmitting then
                            text "Submitting..."
                          else
                            text "Sign Waiver"
                        ]
                    ]
                ]
            ]
        ]