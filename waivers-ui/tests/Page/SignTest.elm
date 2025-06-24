module Page.SignTest exposing (..)

import Expect
import Test exposing (..)
import Html
import Html.Attributes as Attr
import Test.Html.Query as Query
import Test.Html.Event as Event
import Test.Html.Selector as Selector
import Page.Sign as Sign
import Generated.IoBryzekWaiversApi as Api
import Generated.ApiRequest as ApiRequest exposing (ApiRequest(..))
import Url


-- Test data
testUrl : Url.Url
testUrl =
    { protocol = Url.Http
    , host = "localhost"
    , port_ = Just 8080
    , path = "/sign/sig-123"
    , query = Just "pdf=https%3A%2F%2Fexample.com%2Ftest.pdf"
    , fragment = Nothing
    }


testSignature : Api.Signature
testSignature =
    { id = "sig-123"
    , user = 
        { id = "usr-456"
        , email = "test@example.com"
        , firstName = "Test"
        , lastName = "User"
        , phone = Nothing
        }
    , waiver = 
        { id = "wvr-789"
        , projectId = "prj-abc"
        , version = 1
        , title = "Test Waiver"
        , content = "Test waiver content"
        , isCurrent = True
        }
    , status = Api.SignatureStatusSigned
    , signedAt = Nothing
    , signnowUrl = Nothing
    }


suite : Test
suite =
    describe "Page.Sign"
        [ describe "initial state"
            [ test "initializes with NotAsked signature request" <|
                \_ ->
                    let
                        model = Sign.init "sig-123" testUrl
                    in
                    Expect.equal model.signatureRequest NotAsked
            
            , test "extracts PDF URL from query parameters" <|
                \_ ->
                    let
                        model = Sign.init "sig-123" testUrl
                    in
                    Expect.equal model.pdfUrl (Just "https://example.com/test.pdf")
            
            , test "starts with empty signature data" <|
                \_ ->
                    let
                        model = Sign.init "sig-123" testUrl
                    in
                    Expect.equal model.signatureData ""
            ]
        
        , describe "signature submission flow"
            [ test "enters Loading state when signature is submitted" <|
                \_ ->
                    let
                        initialModel = Sign.init "sig-123" testUrl
                        modelWithData = { initialModel | signatureData = "Test User" }
                        (updatedModel, _) = Sign.update Sign.SubmitSignature modelWithData
                    in
                    case updatedModel.signatureRequest of
                        Loading -> Expect.pass
                        _ -> Expect.fail "Expected Loading state after submission"
            
            , test "transitions to Success state when API returns success" <|
                \_ ->
                    let
                        initialModel = Sign.init "sig-123" testUrl
                        apiResult = Ok testSignature
                        (updatedModel, _) = Sign.update (Sign.SignatureSubmitted apiResult) initialModel
                    in
                    case updatedModel.signatureRequest of
                        Success signature -> 
                            Expect.equal signature.id "sig-123"
                        _ -> 
                            Expect.fail "Expected Success state with signature"
            
            , test "transitions to Failure state when API returns error" <|
                \_ ->
                    let
                        initialModel = Sign.init "sig-123" testUrl
                        apiError = ApiRequest.ApiErrorNotFound
                        apiResult = Err apiError
                        (updatedModel, _) = Sign.update (Sign.SignatureSubmitted apiResult) initialModel
                    in
                    case updatedModel.signatureRequest of
                        Failure error -> 
                            Expect.equal error ApiRequest.ApiErrorNotFound
                        _ -> 
                            Expect.fail "Expected Failure state with error"
            ]
        
        , describe "view rendering"
            [ test "shows signing form in initial state" <|
                \_ ->
                    let
                        model = Sign.init "sig-123" testUrl
                        html = Sign.view model
                    in
                    html
                        |> Query.fromHtml
                        |> Query.find [ Selector.attribute (Attr.type_ "text") ]
                        |> Query.has [ Selector.attribute (Attr.placeholder "Enter your full name") ]
            
            , test "shows loading state during submission" <|
                \_ ->
                    let
                        model = Sign.init "sig-123" testUrl
                        loadingModel = { model | signatureRequest = Loading }
                        html = Sign.view loadingModel
                    in
                    html
                        |> Query.fromHtml
                        |> Query.contains [ Html.text "Please wait while we process your signature" ]
            
            , test "shows success message after successful submission" <|
                \_ ->
                    let
                        model = Sign.init "sig-123" testUrl
                        successModel = 
                            { model 
                            | signatureRequest = Success testSignature
                            , pdfUrl = Just "https://example.com/signed.pdf"
                            }
                        html = Sign.view successModel
                    in
                    html
                        |> Query.fromHtml
                        |> Query.contains [ Html.text "Waiver Signed Successfully!" ]
            
            , test "shows download link in success state when PDF URL is available" <|
                \_ ->
                    let
                        model = Sign.init "sig-123" testUrl
                        successModel = 
                            { model 
                            | signatureRequest = Success testSignature
                            , pdfUrl = Just "https://example.com/signed.pdf"
                            }
                        html = Sign.view successModel
                    in
                    html
                        |> Query.fromHtml
                        |> Query.find [ Selector.attribute (Attr.download ("waiver-" ++ testSignature.id ++ ".pdf")) ]
                        |> Query.has [ Selector.attribute (Attr.href "https://example.com/signed.pdf") ]
            
            , test "download link contains correct text" <|
                \_ ->
                    let
                        model = Sign.init "sig-123" testUrl
                        successModel = 
                            { model 
                            | signatureRequest = Success testSignature
                            , pdfUrl = Just "https://example.com/signed.pdf"
                            }
                        html = Sign.view successModel
                    in
                    html
                        |> Query.fromHtml
                        |> Query.find [ Selector.attribute (Attr.download ("waiver-" ++ testSignature.id ++ ".pdf")) ]
                        |> Query.contains [ Html.text "Download Signed Waiver" ]
            
            , test "shows error message in failure state" <|
                \_ ->
                    let
                        model = Sign.init "sig-123" testUrl
                        errorModel = { model | signatureRequest = Failure ApiRequest.ApiErrorNotFound }
                        html = Sign.view errorModel
                    in
                    html
                        |> Query.fromHtml
                        |> Query.contains [ Html.text "Signature not found" ]
            ]
        
        , describe "form interactions"
            [ test "Sign Waiver button is disabled when signature data is empty" <|
                \_ ->
                    let
                        model = Sign.init "sig-123" testUrl
                        html = Sign.view model
                    in
                    html
                        |> Query.fromHtml
                        |> Query.find [ Selector.attribute (Attr.type_ "button") ]
                        |> Query.has [ Selector.attribute (Attr.disabled True) ]
            
            , test "Sign Waiver button is enabled when signature data is provided" <|
                \_ ->
                    let
                        model = Sign.init "sig-123" testUrl
                        modelWithData = { model | signatureData = "Test User" }
                        html = Sign.view modelWithData
                    in
                    html
                        |> Query.fromHtml
                        |> Query.find [ Selector.attribute (Attr.type_ "button") ]
                        |> Query.has [ Selector.attribute (Attr.disabled False) ]
            
            , test "typing in signature input updates model" <|
                \_ ->
                    let
                        model = Sign.init "sig-123" testUrl
                        (updatedModel, _) = Sign.update (Sign.SignatureDataChanged "John Doe") model
                    in
                    Expect.equal updatedModel.signatureData "John Doe"
            ]
        ]