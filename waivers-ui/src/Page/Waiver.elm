module Page.Waiver exposing (Model, Msg, init, update, view)

import Constants
import Generated.ApiRequest as ApiRequest exposing (ApiError, ApiRequest, ApiResult)
import Generated.IoBryzekWaiversApi as Api exposing (Project, Signature, WaiverForm)
import Html exposing (Html, div, form, h1, h2, p, text)
import Html.Attributes as Attr exposing (class)
import Html.Events exposing (onSubmit)
import Http
import Ports exposing (redirectToExternalUrl)
import Templates.Forms as Forms
import Templates.Shell as Shell


type alias Model =
    { slug : String
    , project : ApiRequest Project
    , firstName : String
    , lastName : String
    , email : String
    , phone : String
    , signatureResponse : ApiRequest Signature
    }


type Msg
    = ProjectLoaded (ApiResult Project)
    | FirstNameChanged String
    | LastNameChanged String
    | EmailChanged String
    | PhoneChanged String
    | SignWaiverClicked
    | SignatureResponse (ApiResult Signature)


init : String -> ( Model, Cmd Msg )
init slug =
    ( { slug = slug
      , project = ApiRequest.Loading
      , firstName = ""
      , lastName = ""
      , email = ""
      , phone = ""
      , signatureResponse = ApiRequest.NotAsked
      }
    , loadProject slug
    )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ProjectLoaded result ->
            case result of
                Ok project ->
                    ( { model | project = ApiRequest.Success project }, Cmd.none )

                Err error ->
                    ( { model | project = ApiRequest.Failure error }, Cmd.none )

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
                    , phone =
                        if String.isEmpty model.phone then
                            Nothing

                        else
                            Just model.phone
                    }

                httpParams =
                    { apiHost = Constants.apiHost
                    , headers = []
                    }
            in
            ( { model | signatureResponse = ApiRequest.Loading }
            , createSignatureRequest model.slug waiverForm httpParams
            )

        SignatureResponse result ->
            case result of
                Ok signature ->
                    case signature.signnowUrl of
                        Just url ->
                            ( { model | signatureResponse = ApiRequest.Success signature }
                            , redirectToExternalUrl url
                            )

                        Nothing ->
                            ( { model | signatureResponse = ApiRequest.Success signature }
                            , Cmd.none
                            )

                Err error ->
                    ( { model | signatureResponse = ApiRequest.Failure error }
                    , Cmd.none
                    )


loadProject : String -> Cmd Msg
loadProject slug =
    Http.get
        { url = Constants.apiHost ++ "/projects/" ++ slug
        , expect = ApiRequest.expectJson ProjectLoaded Api.projectDecoder
        }


createSignatureRequest : String -> WaiverForm -> Api.HttpRequestParams -> Cmd Msg
createSignatureRequest slug waiverForm params =
    let
        url =
            params.apiHost ++ "/projects/" ++ slug ++ "/signatures"
    in
    Http.request
        { method = "POST"
        , url = url
        , expect = ApiRequest.expectJson SignatureResponse Api.signatureDecoder
        , headers = params.headers
        , timeout = Nothing
        , tracker = Nothing
        , body = Http.jsonBody (Api.waiverFormEncoder waiverForm)
        }


apiErrorToString : ApiError -> String
apiErrorToString error =
    case error of
        ApiRequest.ApiErrorSystem msg ->
            "System error: " ++ msg

        ApiRequest.ApiErrorUnsupportedStatusCode code ->
            "Server error (code " ++ String.fromInt code ++ ")"

        ApiRequest.ApiErrorJsonParse msg ->
            "Invalid response format: " ++ msg

        ApiRequest.ApiErrorNotFound ->
            "Resource not found"

        ApiRequest.ApiErrorNotAuthorized ->
            "You are not authorized to perform this action"

        ApiRequest.ApiErrorValidation errors ->
            "Validation errors: " ++ String.join ", " (List.map .message errors)


view : Model -> Maybe Int -> Html Msg
view model currentYear =
    Shell.view
        { title = "Sign Waiver"
        , currentYear = currentYear
        , content =
            [ div [ class "max-w-2xl mx-auto" ]
                [ case model.project of
                    ApiRequest.Loading ->
                        div [ class "bg-white p-6 rounded-lg shadow-md text-center" ]
                            [ div [ class "animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4" ] []
                            , p [ class "text-gray-600" ] [ text "Loading project..." ]
                            ]

                    ApiRequest.Failure error ->
                        div [ class "bg-white p-6 rounded-lg shadow-md" ]
                            [ div [ class "bg-red-50 border border-red-200 rounded-md p-4" ]
                                [ h2 [ class "text-lg font-semibold text-red-800 mb-2" ]
                                    [ text "Project Not Found" ]
                                , p [ class "text-red-700" ]
                                    [ text
                                        (case error of
                                            ApiRequest.ApiErrorNotFound ->
                                                "The project '" ++ model.slug ++ "' could not be found. Please check the URL and try again."

                                            _ ->
                                                "Error loading project: " ++ apiErrorToString error
                                        )
                                    ]
                                ]
                            ]

                    ApiRequest.Success project ->
                        div []
                            [ h1 [ class "text-3xl font-bold text-gray-900 mb-6" ]
                                [ text ("Sign Waiver: " ++ project.name) ]
                            , viewWaiverForm model
                            ]

                    ApiRequest.NotAsked ->
                        div [ class "bg-white p-6 rounded-lg shadow-md text-center" ]
                            [ p [ class "text-gray-600" ] [ text "Initializing..." ] ]
                ]
            ]
        }


viewWaiverForm : Model -> Html Msg
viewWaiverForm model =
    div [ class "bg-white p-6 rounded-lg shadow-md" ]
        [ h2 [ class "text-xl font-semibold text-gray-900 mb-4" ]
            [ text "Waiver Agreement" ]
        , div [ class "prose text-gray-700 mb-6" ]
            [ p []
                [ text "By signing this waiver, you acknowledge that you understand and agree to the terms and conditions of participation. This includes assuming all risks associated with the activity and releasing the organization from liability." ]
            , p []
                [ text "Please fill out your information below and click 'Sign Waiver' to proceed with digital signature via HelloSign." ]
            ]
        , case model.signatureResponse of
            ApiRequest.Failure error ->
                div [ class "bg-red-50 border border-red-200 rounded-md p-4 mb-4" ]
                    [ div [ class "flex" ]
                        [ div [ class "text-sm text-red-700" ]
                            [ text (apiErrorToString error) ]
                        ]
                    ]

            _ ->
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
                    isValid =
                        not (String.isEmpty model.firstName || String.isEmpty model.lastName || String.isEmpty model.email)

                    isSubmitting =
                        ApiRequest.isLoading model.signatureResponse

                    buttonText =
                        if isSubmitting then
                            "Processing..."

                        else if not isValid then
                            "Please fill required fields"

                        else
                            "Sign Waiver"
                  in
                  Html.button
                    [ Html.Events.onClick SignWaiverClicked
                    , Attr.disabled (isSubmitting || not isValid)
                    , Attr.class <|
                        if isValid && not isSubmitting then
                            "px-4 py-2 bg-blue-600 text-white font-medium rounded-md shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"

                        else
                            "px-4 py-2 bg-gray-300 text-gray-500 font-medium rounded-md shadow-sm cursor-not-allowed"
                    ]
                    [ text buttonText ]
                ]
            ]
        ]
