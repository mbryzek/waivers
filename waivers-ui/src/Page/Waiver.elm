module Page.Waiver exposing (Model, Msg, init, update, view)

import Generated.ApiRequest exposing (ApiError, ApiResult)
import Generated.IoBryzekWaiversApi as Api exposing (Signature, WaiverForm)
import Html exposing (Html, div, h1, h2, p, text, form)
import Html.Attributes as Attr exposing (class)
import Html.Events exposing (onSubmit)
import Http
import Ports exposing (redirectToExternalUrl)
import Templates.Forms as Forms
import Templates.Shell as Shell


type alias Model =
    { slug : String
    , firstName : String
    , lastName : String
    , email : String
    , phone : String
    , isSubmitting : Bool
    , error : Maybe String
    }


type Msg
    = FirstNameChanged String
    | LastNameChanged String
    | EmailChanged String
    | PhoneChanged String
    | SignWaiverClicked
    | SignatureResponse (ApiResult Signature)


init : String -> ( Model, Cmd Msg )
init slug =
    ( { slug = slug
      , firstName = ""
      , lastName = ""
      , email = ""
      , phone = ""
      , isSubmitting = False
      , error = Nothing
      }
    , Cmd.none
    )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        FirstNameChanged value ->
            ( { model | firstName = value }, Cmd.none )

        LastNameChanged value ->
            ( { model | lastName = value }, Cmd.none )

        EmailChanged value ->
            ( { model | email = value }, Cmd.none )

        PhoneChanged value ->
            ( { model | phone = value }, Cmd.none )

        SignWaiverClicked ->
            let
                waiverForm : WaiverForm
                waiverForm =
                    { firstName = model.firstName
                    , lastName = model.lastName
                    , email = model.email
                    , phone = if String.isEmpty model.phone then Nothing else Just model.phone
                    }

                httpParams =
                    { apiHost = "http://localhost:9300"
                    , headers = []
                    }
            in
            ( { model | isSubmitting = True, error = Nothing }
            , createSignatureRequest model.slug waiverForm httpParams
            )

        SignatureResponse result ->
            case result of
                Ok signature ->
                    case signature.signnowUrl of
                        Just url ->
                            ( { model | isSubmitting = False }
                            , redirectToExternalUrl url
                            )

                        Nothing ->
                            ( { model | isSubmitting = False, error = Just "No signature URL received" }
                            , Cmd.none
                            )

                Err error ->
                    ( { model | isSubmitting = False, error = Just (apiErrorToString error) }
                    , Cmd.none
                    )


createSignatureRequest : String -> WaiverForm -> Api.HttpRequestParams -> Cmd Msg
createSignatureRequest slug waiverForm params =
    Http.request
        { method = "POST"
        , url = params.apiHost ++ "/projects/" ++ slug ++ "/signatures"
        , expect = Generated.ApiRequest.expectJson SignatureResponse Api.signatureDecoder
        , headers = params.headers
        , timeout = Nothing
        , tracker = Nothing
        , body = Http.jsonBody (Api.waiverFormEncoder waiverForm)
        }


apiErrorToString : ApiError -> String
apiErrorToString error =
    case error of
        Generated.ApiRequest.ApiErrorSystem msg ->
            "System error: " ++ msg

        Generated.ApiRequest.ApiErrorUnsupportedStatusCode code ->
            "Server error (code " ++ String.fromInt code ++ ")"

        Generated.ApiRequest.ApiErrorJsonParse msg ->
            "Invalid response format: " ++ msg

        Generated.ApiRequest.ApiErrorNotFound ->
            "Resource not found"

        Generated.ApiRequest.ApiErrorNotAuthorized ->
            "You are not authorized to perform this action"

        Generated.ApiRequest.ApiErrorValidation errors ->
            "Validation errors: " ++ String.join ", " (List.map .message errors)


view : Model -> Html Msg
view model =
    Shell.view
        { title = "Sign Waiver"
        , content =
            [ div [ class "max-w-2xl mx-auto" ]
                [ h1 [ class "text-3xl font-bold text-gray-900 mb-6" ]
                    [ text ("Sign Waiver: " ++ model.slug) ]
                , div [ class "bg-white p-6 rounded-lg shadow-md" ]
                    [ h2 [ class "text-xl font-semibold text-gray-900 mb-4" ]
                        [ text "Waiver Agreement" ]
                    , div [ class "prose text-gray-700 mb-6" ]
                        [ p []
                            [ text "By signing this waiver, you acknowledge that you understand and agree to the terms and conditions of participation. This includes assuming all risks associated with the activity and releasing the organization from liability." ]
                        , p []
                            [ text "Please fill out your information below and click 'Sign Waiver' to proceed with digital signature via HelloSign." ]
                        ]
                    , case model.error of
                        Just errorMsg ->
                            div [ class "bg-red-50 border border-red-200 rounded-md p-4 mb-4" ]
                                [ div [ class "flex" ]
                                    [ div [ class "text-sm text-red-700" ]
                                        [ text errorMsg ]
                                    ]
                                ]

                        Nothing ->
                            text ""
                    , form [ onSubmit SignWaiverClicked ]
                        [ Forms.formGroup "First Name"
                            (Forms.textInput
                                { value = model.firstName
                                , placeholder = "Enter your first name"
                                , onInput = FirstNameChanged
                                }
                            )
                        , Forms.formGroup "Last Name"
                            (Forms.textInput
                                { value = model.lastName
                                , placeholder = "Enter your last name"
                                , onInput = LastNameChanged
                                }
                            )
                        , Forms.formGroup "Email Address"
                            (Forms.textInput
                                { value = model.email
                                , placeholder = "Enter your email address"
                                , onInput = EmailChanged
                                }
                            )
                        , Forms.formGroup "Phone Number (Optional)"
                            (Forms.textInput
                                { value = model.phone
                                , placeholder = "Enter your phone number"
                                , onInput = PhoneChanged
                                }
                            )
                        , div [ class "flex justify-end pt-4" ]
                            [ let
                                isValid = not (String.isEmpty model.firstName || String.isEmpty model.lastName || String.isEmpty model.email)
                                buttonText = 
                                    if model.isSubmitting then 
                                        "Processing..." 
                                    else if not isValid then
                                        "Please fill required fields"
                                    else 
                                        "Sign Waiver"
                              in
                              Html.button
                                [ Html.Events.onClick SignWaiverClicked
                                , Attr.disabled (model.isSubmitting || not isValid)
                                , Attr.class <|
                                    if isValid && not model.isSubmitting then
                                        "px-4 py-2 bg-blue-600 text-white font-medium rounded-md shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                                    else
                                        "px-4 py-2 bg-gray-300 text-gray-500 font-medium rounded-md shadow-sm cursor-not-allowed"
                                ]
                                [ text buttonText ]
                            ]
                        ]
                    ]
                ]
            ]
        }