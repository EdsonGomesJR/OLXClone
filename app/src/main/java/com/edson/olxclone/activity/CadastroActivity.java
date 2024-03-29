package com.edson.olxclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.edson.olxclone.R;
import com.edson.olxclone.helper.ConfiguracaoFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso;

    private FirebaseAuth autenticacao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializarComponentes();
        loginCadastro();

    }

    private void inicializarComponentes(){

        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        botaoAcessar = findViewById(R.id.buttonAcesso);
        tipoAcesso = findViewById(R.id.switchAcesso);

    }

    private void loginCadastro(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if(!email.isEmpty()){
                    if(!senha.isEmpty()){

                        //verifica o status do switch
                        if(tipoAcesso.isChecked()){
                            //cadastro

                            autenticacao.createUserWithEmailAndPassword(
                                    email, senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){

                                        Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));
                                    }else{
                                        String erroExcecao = "";

                                        try {
                                            throw task.getException();
                                        }catch (FirebaseAuthWeakPasswordException e){

                                            erroExcecao = "Digite uma senha mais forte!";
                                        }catch (FirebaseAuthInvalidCredentialsException e ){
                                            erroExcecao = "Por favor, digite um e-mail válido";
                                        }catch ( FirebaseAuthUserCollisionException e){
                                            erroExcecao = "Essa conta ja foi registrada";
                                        }catch (Exception e){

                                            erroExcecao = "ao cadastrar usuário:    " + e.getMessage();
                                            e.printStackTrace();
                                        }

                                        Toast.makeText(CadastroActivity.this, "Erro: " + erroExcecao, Toast.LENGTH_SHORT).show();
                                    }

                                }

                            });
                        }
                        else{

                            //login

                            autenticacao.signInWithEmailAndPassword(email,senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){

                                        Toast.makeText(CadastroActivity.this, "LOGADO COM SUCESSO", Toast.LENGTH_SHORT).show();

                                        startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));
                                    } else{

                                        Toast.makeText(CadastroActivity.this, "ERRO AO FAZER O LOGIN:   "
                                                + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }

                    }else{

                        Toast.makeText(CadastroActivity.this, "Preencha a senha", Toast.LENGTH_SHORT).show();
                    }


                } else{
                    Toast.makeText(CadastroActivity.this, "Preencha o e-mail", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
